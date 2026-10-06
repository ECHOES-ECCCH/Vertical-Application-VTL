package pl.psnc.vtl.portal.services.verses;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.context.annotation.SessionScope;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.layers.PageLayer.Id;

import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

@Service
@SessionScope
@RequiredArgsConstructor
@Slf4j
public class UndoRedoService {
    private final HashMap<Id, OperationStack> operationStacks = new HashMap<>();

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * Adds an operation to the undo/redo stack. This call is ignored if invoked
     * during undo/redo action itself.
     */
    public void addOperation(Id pageLayerId, Operation operation) {
        OperationStack stack = operationStacks.computeIfAbsent(pageLayerId, OperationStack::new);
        if (stack.undoRedoThread == Thread.currentThread()) {
            // we currently do undo/redo, so don't update undo stack
            return;
        }
        try {
            // make sure no other thread performs unod/redo
            stack.semaphore.acquire();
        } catch (InterruptedException e) {
            log.debug("Thread interrupted, skipping undo stack update", e);
            Thread.currentThread().interrupt();
            return;
        }
        stack.waitingOperation = operation;
        applicationEventPublisher.publishEvent(stack);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    protected void addWaitingOperation(OperationStack stack) {
        stack.subList(stack.currentPosition, stack.size()).clear();
        stack.add(stack.waitingOperation);
        while (stack.size() > 30)
            stack.remove(0);
        stack.currentPosition = (byte) stack.size();
        log.debug("Operation added to undo stack of pageLayer {}, in total: {}", stack.pageLayerId,
                stack.currentPosition);
        releaseWaitingOperation(stack);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    protected void releaseWaitingOperation(OperationStack stack) {
        stack.waitingOperation = null;
        stack.semaphore.release();
    }

    public boolean canUndo(Id pageLayerId) {
        OperationStack stack = operationStacks.get(pageLayerId);
        return stack != null && stack.currentPosition > 0;
    }

    @Transactional
    public void undo(Id pageLayerId) {
        OperationStack stack = operationStacks.get(pageLayerId);
        if (stack == null || stack.currentPosition == 0)
            throw new UndoRedoException("No operation to undo", "undoNotAvailable");
        if (!stack.semaphore.tryAcquire())
            throw new UndoRedoException("Undo or redo action already in progress", "undoRedoInProgress");

        stack.undoRedoThread = Thread.currentThread();
        try {
            Operation operation = stack.get(stack.currentPosition - 1);
            for (int i = operation.undo.size() - 1; i >= 0; i--) {
                operation.undo.get(i).run();
            }
            stack.currentPosition--;
        } catch (Exception e) {
            log.error("Undo failed for pageLayer {}", pageLayerId, e);
        } finally {
            stack.undoRedoThread = null;
            stack.semaphore.release();
        }
    }

    public boolean canRedo(Id pageLayerId) {
        OperationStack stack = operationStacks.get(pageLayerId);
        return stack != null && stack.currentPosition < stack.size();
    }

    @Transactional
    public void redo(Id pageLayerId) {
        OperationStack stack = operationStacks.get(pageLayerId);
        if (stack == null || stack.currentPosition >= stack.size())
            throw new UndoRedoException("No operation to redo", "redoNotAvailable");
        if (!stack.semaphore.tryAcquire())
            throw new UndoRedoException("Undo or redo action already in progress", "undoRedoInProgress");

        stack.undoRedoThread = Thread.currentThread();
        try {
            Operation operation = stack.get(stack.currentPosition);
            operation.redo.forEach(Runnable::run);
            stack.currentPosition++;
        } catch (Exception e) {
            log.error("Undo failed for pageLayer {}", pageLayerId, e);
        } finally {
            stack.undoRedoThread = null;
            stack.semaphore.release();
        }
    }

    public void reset(Id pageLyerId) {
        operationStacks.remove(pageLyerId);
    }

    public static class Operation {
        final List<Runnable> undo = new ArrayList<>();
        final List<Runnable> redo = new ArrayList<>();

        public Operation undo(Runnable runnable) {
            undo.add(runnable);
            return this;
        }

        public Operation redo(Runnable runnable) {
            redo.add(runnable);
            return this;
        }

        public Operation andAlways(Runnable runnable) {
            undo.add(0, runnable);
            redo.add(runnable);
            return this;
        }
    }

    private static class OperationStack extends ArrayList<Operation> {
        final Semaphore semaphore = new Semaphore(1);
        final PageLayer.Id pageLayerId;
        volatile byte currentPosition = 0;
        volatile Thread undoRedoThread;
        volatile Operation waitingOperation;

        public OperationStack(PageLayer.Id pageLayerId) {
            this.pageLayerId = pageLayerId;
        }
    }
}
