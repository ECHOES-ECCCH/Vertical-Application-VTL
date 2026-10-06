package pl.psnc.vtl.portal.services.pages.zip;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public final class SequentialScheduler {

    private static final int OFFLOAD = 1;
    private static final int AGAIN = 2;
    private static final int BEGIN = 4;
    private static final int STOP = 8;
    private static final int END = 16;
    private final AtomicInteger state = new AtomicInteger(16);
    private final SequentialScheduler.RestartableTask restartableTask;
    private final SequentialScheduler.DeferredCompleter completer;
    private final SequentialScheduler.SchedulableTask schedulableTask;


    public SequentialScheduler(SequentialScheduler.RestartableTask restartableTask) {
        this.restartableTask = (SequentialScheduler.RestartableTask) Objects.requireNonNull(restartableTask);
        this.completer = new SequentialScheduler.TryEndDeferredCompleter();
        this.schedulableTask = new SequentialScheduler.SchedulableTask();
    }


    public void runOrSchedule() {
        this.runOrSchedule(this.schedulableTask, (Executor) null);
    }


    public void runOrSchedule(Executor executor) {
        this.runOrSchedule(this.schedulableTask, executor);
    }


    private void runOrSchedule(SequentialScheduler.SchedulableTask task, Executor executor) {
        while (true) {
            int s = this.state.get();
            if (s == 16) {
                if (this.state.compareAndSet(16, 4)) {
                    if (executor == null) {
                        task.run();
                    } else {
                        executor.execute(task);
                    }

                    return;
                }
            } else {
                if ((s & 4) != 0) {
                    if (!this.state.compareAndSet(s, 2 | s & 1)) {
                        continue;
                    }

                    return;
                }

                if ((s & 2) == 0 && s != 8) {
                    throw new InternalError(String.valueOf(s));
                }

                return;
            }
        }
    }


    public boolean isStopped() {
        return this.state.get() == 8;
    }


    public void stop() {
        this.state.set(8);
    }


    public static SequentialScheduler synchronizedScheduler(Runnable mainLoop) {
        return new SequentialScheduler(new SequentialScheduler.SynchronizedRestartableTask(mainLoop));
    }


    private class TryEndDeferredCompleter extends SequentialScheduler.DeferredCompleter {

        private TryEndDeferredCompleter() {
        }


        public void complete() {
            label39:
            while (true) {
                int s;
                if (((s = SequentialScheduler.this.state.get()) & 1) != 0) {
                    if (SequentialScheduler.this.state.compareAndSet(s, s & -2)) {
                        return;
                    }
                } else {
                    for (; (s & 1) == 0; s = SequentialScheduler.this.state.get()) {
                        if (s == 4) {
                            if (SequentialScheduler.this.state.compareAndSet(4, 16)) {
                                return;
                            }
                        } else {
                            if (s != 2) {
                                if (s == 8) {
                                    return;
                                }

                                if (s == 16) {
                                    throw new IllegalStateException("Duplicate completion");
                                }

                                throw new InternalError(String.valueOf(s));
                            }

                            if (SequentialScheduler.this.state.compareAndSet(2, 5)) {
                                SequentialScheduler.this.restartableTask.run(SequentialScheduler.this.completer);
                                continue label39;
                            }
                        }
                    }

                    throw new InternalError(String.valueOf(s));
                }
            }
        }
    }

    private final class SchedulableTask implements Runnable {

        private SchedulableTask() {
        }


        public void run() {
            SequentialScheduler.this.restartableTask.run(SequentialScheduler.this.completer);
        }
    }

    public static final class SynchronizedRestartableTask extends SequentialScheduler.CompleteRestartableTask {

        private final Runnable mainLoop;
        private final Object lock = new Object();


        public SynchronizedRestartableTask(Runnable mainLoop) {
            this.mainLoop = mainLoop;
        }


        protected void run() {
            synchronized (this.lock) {
                this.mainLoop.run();
            }
        }
    }

    public abstract static class CompleteRestartableTask implements SequentialScheduler.RestartableTask {

        public CompleteRestartableTask() {
        }


        public final void run(SequentialScheduler.DeferredCompleter taskCompleter) {
            try {
                this.run();
            } finally {
                taskCompleter.complete();
            }

        }


        protected abstract void run();
    }

    @FunctionalInterface
    public interface RestartableTask {

        void run(SequentialScheduler.DeferredCompleter var1);
    }

    public abstract static class DeferredCompleter {

        private DeferredCompleter() {
        }


        public abstract void complete();
    }
}

