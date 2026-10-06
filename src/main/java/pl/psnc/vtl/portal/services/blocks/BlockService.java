package pl.psnc.vtl.portal.services.blocks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.conf.logging.LogAction;
import pl.psnc.vtl.portal.dto.blocks.BlockConsistencyError;
import pl.psnc.vtl.portal.dto.blocks.BlockDto;
import pl.psnc.vtl.portal.dto.blocks.MultipageBlockRemoveMode;
import pl.psnc.vtl.portal.model.blocks.Block;
import pl.psnc.vtl.portal.model.blocks.BlockType;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.layers.PageLayer.Id;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.repositories.blocks.BlockRepository;
import pl.psnc.vtl.portal.repositories.pages.PageLayersRepository;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.services.iiifserver.IIIFUriResolver;
import pl.psnc.vtl.portal.services.imports.BeanLoader;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.transcriptions.ModificationTrackingService;
import pl.psnc.vtl.portal.services.verses.UndoRedoService;
import pl.psnc.vtl.portal.services.verses.VerseService;
import pl.psnc.vtl.portal.services.verses.UndoRedoService.Operation;

import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.lang.Math.signum;
import static java.util.stream.Collectors.toList;
import static pl.psnc.vtl.portal.dto.blocks.BlockDto.onlyBlocks;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BlockService {

    private final TranscriptionPermissionService transcriptionPermissionService;
    private final ModificationTrackingService modificationTrackingService;

    private final BlockRepository blockRepository;
    private final PageLayersRepository pageLayersRepository;
    private final PageRepository pageRepository;
    private final VerseRepository verseRepository;

    private final IIIFUriResolver iiifUriResolver;

    private final UndoRedoService undoRedoService;

    public void addBlock(long transcriptionId, Id pageLayerId, BlockDto block) {
        if (block.getFirstVerseNo() < 1)
            throw new ValidationException("firstVerseNo must be at least 1");
        if (block.getFirstVerseNo() > block.getLastVerseNo())
            throw new ValidationException("firstVerseNo must not be greater than lastVerseNo");

        PageLayer pageLayer = pageLayerForAction(transcriptionId, pageLayerId);
        int versesCount = verseRepository.countByPageLayer(pageLayer);
        if (block.getLastVerseNo() > versesCount)
            throw new ValidationException("Block last verse out of range with " + versesCount + " verses");
        List<BlockDto> allBlocks =
                blockRepository.getByPageLayer(pageLayer).stream().map(BlockDto::from).collect(toList());

        Block b = new Block();
        b.setType(block.getType());
        b.setFirstVerseNo(block.getFirstVerseNo());
        b.setLastVerseNo(block.getLastVerseNo());
        b.setPageLayer(pageLayer);
        b = blockRepository.save(b);
        allBlocks.add(BlockDto.from(b));

        try {
            getStructure(allBlocks);
        } catch (IllegalArgumentException e) {
            log.debug("Can't add block", e);
            throw new ValidationException(e.getMessage());
        }

        Block undoRedoBlock = b;
        undoRedoService.addOperation(pageLayerId, new Operation()
                .redo(() -> blockRepository.save(undoRedoBlock))
                .undo(() -> blockRepository.delete(undoRedoBlock)));

        LogAction.MODIFY_TRANSCRIPTION.info(log, "Created block with id {} for {}", b.getId(), pageLayerId);
    }

    @Transactional(readOnly = true)
    public List<Object> getBlocks(long transcriptionId, Id pageLayerId) {
        PageLayer pageLayer = pageLayersRepository.findById(pageLayerId).orElse(null);
        if (pageLayer == null || transcriptionId != pageLayer.getPage().getTranscription().getId())
            throw new EntityNotFoundException("No such page-layer");
        transcriptionPermissionService.checkReadAccess(transcriptionId);

        return getFullStructure(pageLayer, blockRepository.getByPageLayer(pageLayer),
                BeanLoader.getBean(VerseService.class).completeVerses(pageLayer));
    }

    @Transactional(readOnly = true)
    public List<Object> getBlocks(PageLayer.Id pageLayerId) {
        PageLayer pageLayer = pageLayersRepository.findById(pageLayerId).orElse(null);
        return getFullStructure(pageLayer, blockRepository.getByPageLayer(pageLayer),
                BeanLoader.getBean(VerseService.class).completeVerses(pageLayer));
    }

    public List<Object> getBlocks(PageLayer pageLayer, List<Verse> verses) {
        return getFullStructure(pageLayer, blockRepository.getByPageLayer(pageLayer),
                verses.stream().filter(v -> v.getPageLayer().getId() == pageLayer.getId()).collect(toList()));
    }

    public List<BlockDto> getBlocksForExport(Page page, Layer layer) {
        if (page.getVerses() == null) {
            List<Verse> verses = BeanLoader.getBean(VerseService.class).completeVerses(page);
            page.setVerses(verses);

            page.getPageLayers().forEach(pageLayer -> pageLayer.setVerses(
                    verses.stream().filter(v -> v.getPageLayer().getId().equals(pageLayer.getId())).collect(toList())));
        }

        List<Object> items = getBlocks(page.getTranscription().getId(), new PageLayer.Id(page.getId(), layer.getId()));
        processImageBlocks(items);
        List<Verse> dummyParagraph = new ArrayList<>();
        List<BlockDto> result = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Verse) {
                dummyParagraph.add((Verse) item);
                continue;
            }
            if (!dummyParagraph.isEmpty()) {
                result.add(new BlockDto(BlockType.PARAGRAPH, dummyParagraph));
                dummyParagraph.clear();
            }
            result.add((BlockDto) item);
        }
        if (!dummyParagraph.isEmpty()) {
            result.add(new BlockDto(BlockType.PARAGRAPH, dummyParagraph));
        }
        return result;
    }

    private void processImageBlocks(List<Object> items) {
        for (Object item : items) {
            if (!(item instanceof BlockDto))
                continue;
            BlockDto block = (BlockDto) item;
            if (block.getType() == BlockType.IMAGE) {
                for (Object child : block.getChildren()) {
                    if (!(child instanceof Verse))
                        continue;
                    Verse v = (Verse) child;
                    v.setContent(iiifUriResolver.getPublicImageUrl(v));
                    v.setImage(true);
                }
            }
            processImageBlocks(block.getChildren());
        }
    }

    public void removeBlock(long transcriptionId, Id pageLayerId, long blockId,
            MultipageBlockRemoveMode multipageMode) {
        PageLayer pageLayer = pageLayerForAction(transcriptionId, pageLayerId);

        List<Block> allBlocks = blockRepository.getByPageLayer(pageLayer);
        int removedIndex = IntStream.range(0, allBlocks.size())
                .filter(i -> allBlocks.get(i).getId() == blockId)
                .findAny().orElseThrow(() -> new EntityNotFoundException("No such block"));
        Block blockToRemove = allBlocks.remove(removedIndex);
        blockRepository.delete(blockToRemove);

        if (multipageMode == null || multipageMode == MultipageBlockRemoveMode.SINGLE) {
            undoRedoService.addOperation(pageLayerId, new Operation()
                    .redo(() -> blockRepository.delete(blockToRemove))
                    .undo(() -> blockRepository.save(blockToRemove)));
        } else {
            Consumer<Block> backwardConsumer = multipageMode == MultipageBlockRemoveMode.ALL
                    ? blockRepository::delete : b -> {/* empty */};
            traverseContinuation(blockToRemove, backwardConsumer, blockRepository::delete);
        }

        LogAction.MODIFY_TRANSCRIPTION.info(log, "Removed block with id {}", blockId);
    }

    public void changeBlockContinuation(long transcriptionId, Id pageLayerId, long blockId) {
        PageLayer pageLayer = pageLayerForAction(transcriptionId, pageLayerId);
        List<Block> allBlocks = blockRepository.getByPageLayer(pageLayer);
        List<BlockDto> allBlockDtos = allBlocks.stream().map(BlockDto::from).collect(toList());
        BlockDto blockDto = allBlockDtos.stream().filter(b -> b.getBlockId() == blockId).findAny()
                .orElseThrow(() -> new ValidationException("No such root block"));

        checkPotentialContinuation(pageLayer, getStructure(allBlockDtos));
        if (!blockDto.isContinuation() && !blockDto.isPotentialContinuation())
            throw new ValidationException("Continuation inconsistency detected");

        doChangeBlockContinuation(blockId, !blockDto.isContinuation());

        undoRedoService.addOperation(pageLayerId, new Operation()
                .redo(() -> doChangeBlockContinuation(blockId, !blockDto.isContinuation()))
                .undo(() -> doChangeBlockContinuation(blockId, blockDto.isContinuation())));

        LogAction.MODIFY_TRANSCRIPTION.info(log, "Changed continuation of block with id {}", blockId);
    }

    private void doChangeBlockContinuation(long blockId, boolean continuation) {
        Block block = blockRepository.findById(blockId).orElseThrow(() -> new EntityNotFoundException("No such block"));
        block.setContinuation(continuation);
        blockRepository.save(block);
        LogAction.MODIFY_TRANSCRIPTION.info(log, "Changed continuation of block with id {} to {}", blockId,
                continuation);
    }

    public void removeContinuationMultipage(long transcriptionId, Id pageLayerId, long blockId) {
        pageLayerForAction(transcriptionId, pageLayerId);
        Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new EntityNotFoundException("No such block"));

        block.setContinuation(false);

        traverseContinuation(block, b -> b.setContinuation(false), b -> b.setContinuation(false));

        LogAction.MODIFY_TRANSCRIPTION.info(log, "Removed continuation multipage of block with id {}", blockId);
    }

    private void traverseContinuation(Block block, Consumer<Block> backwardConsumer, Consumer<Block> forwardConsumer) {
        long transcriptionId = block.getPageLayer().getPage().getTranscription().getId();
        long layerId = block.getPageLayer().getLayer().getId();
        int pageNo = block.getPageLayer().getPage().getPageNo();
        for (int i = pageNo - 1; i > 0; i--) {
            Page p = pageRepository.findByTranscriptionIdAndPageNo(transcriptionId, i);
            PageLayer.Id plId = new PageLayer.Id(p.getId(), layerId);
            List<Block> blocks = blockRepository.getByPageLayerIdAndType(plId, block.getType());
            if (blocks.isEmpty())
                break;
            boolean wasContinuation = blocks.get(blocks.size() - 1).isContinuation();
            backwardConsumer.accept(blocks.get(blocks.size() - 1));
            if (blocks.size() > 1 || !wasContinuation)
                break;
        }
        for (int i = pageNo + 1; i <= pageRepository.countByTranscriptionId(transcriptionId); i++) {
            Page p = pageRepository.findByTranscriptionIdAndPageNo(transcriptionId, i);
            PageLayer.Id plId = new PageLayer.Id(p.getId(), layerId);
            List<Block> blocks = blockRepository.getByPageLayerIdAndType(plId, block.getType());
            if (blocks.isEmpty() || !blocks.get(0).isContinuation())
                break;
            forwardConsumer.accept(blocks.get(0));
            if (blocks.size() > 1)
                break;
        }
    }

    public void addMultipageBlock(long transcriptionId, Id pageLayerId, long blockId, int fromPage,
            int toPage) {
        PageLayer pageLayer = pageLayerForAction(transcriptionId, pageLayerId);
        int lastPageNo = pageRepository.countByTranscriptionId(transcriptionId) + 1;
        if (fromPage < 1 || toPage > lastPageNo || fromPage > toPage)
            throw new ValidationException("invalid pages range " + fromPage + "-" + toPage);
        int pageNo = pageLayer.getPage().getPageNo();
        if (pageNo < fromPage || pageNo > toPage)
            throw new ValidationException("fromPage and toPage must contain page with selected block");
        Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new EntityNotFoundException("No such block"));

        if (pageNo > fromPage)
            block.setContinuation(true);

        for (int i = fromPage; i <= toPage; i++) {
            if (i == pageNo)
                continue;
            Page p = pageRepository.findByTranscriptionIdAndPageNo(transcriptionId, i);
            PageLayer pl = pageLayersRepository.getById(new PageLayer.Id(p.getId(), pageLayerId.getLayerId()));
            Block b = new Block();
            b.setType(block.getType());
            b.setContinuation(i != fromPage);
            b.setFirstVerseNo(1);
            b.setLastVerseNo(verseRepository.countByPageLayer(pl) + 1);
            b.setPageLayer(pl);
            blockRepository.save(b);
        }

        LogAction.MODIFY_TRANSCRIPTION.info(log, "Added multipage block around block {} from page {} to {}",
                blockId, fromPage, toPage);
    }

    private PageLayer pageLayerForAction(long transcriptionId, Id pageLayerId) {
        PageLayer pageLayer = pageLayersRepository.findById(pageLayerId).orElse(null);
        if (pageLayer == null || transcriptionId != pageLayer.getPage().getTranscription().getId())
            throw new EntityNotFoundException("No such page-layer");
        transcriptionPermissionService.checkTranscribeAccess(transcriptionId);
        modificationTrackingService.trackModification(pageLayerId);
        return pageLayer;
    }

    public void versesMoved(List<Verse> verses, int distance, Operation undoRedoOp) {
        Map<Long, Integer> blockToDist = new HashMap<>();
        List<Block> blocks = blockRepository.getByPageLayer(verses.get(0).getPageLayer());
        int movedFirstNo = verses.get(0).getVerseNo();
        int movedLastNo = verses.get(verses.size() - 1).getVerseNo();
        int otherFirstNo = distance < 0 ? movedFirstNo + distance : movedLastNo + 1;
        int otherLastNo = distance < 0 ? movedFirstNo - 1 : movedLastNo + distance;
        for (Iterator<Block> it = blocks.iterator(); it.hasNext();) {
            Block b = it.next();
            if (b.getFirstVerseNo() + distance >= movedFirstNo && b.getLastVerseNo() + distance <= movedLastNo) {
                blockToDist.put(b.getId(), distance);
            } else if (b.getFirstVerseNo() + distance >= otherFirstNo && b.getLastVerseNo() + distance <= otherLastNo) {
                blockToDist.put(b.getId(), (int) (signum(-distance) * verses.size()));
            } else {
                it.remove();
            }
        }
        changeAll(blocks, b -> b.setFirstVerseNo(b.getFirstVerseNo() + blockToDist.get(b.getId())));
        changeAll(blocks, b -> b.setLastVerseNo(b.getLastVerseNo() + blockToDist.get(b.getId())));

        undoRedoOp.redo(() -> {
            changeAll(blocks, b -> b.setFirstVerseNo(b.getFirstVerseNo() + blockToDist.get(b.getId())));
            changeAll(blocks, b -> b.setLastVerseNo(b.getLastVerseNo() + blockToDist.get(b.getId())));
        }).undo(() -> {
            changeAll(blocks, b -> b.setFirstVerseNo(b.getFirstVerseNo() - blockToDist.get(b.getId())));
            changeAll(blocks, b -> b.setLastVerseNo(b.getLastVerseNo() - blockToDist.get(b.getId())));
        });
    }

    public void verseAdded(Verse verse, Operation undoRedoOp) {
        List<Block> blocks = blockRepository.getByPageLayer(verse.getPageLayer());
        long vNo = verse.getVerseNo();
        List<Block> forFirstUpdates = blocks.stream().filter(b -> b.getFirstVerseNo() >= vNo).collect(toList());
        List<Block> forLastUpdates = blocks.stream().filter(b -> b.getLastVerseNo() >= vNo).collect(toList());
        changeAll(forFirstUpdates, b -> b.setFirstVerseNo(b.getFirstVerseNo() + 1));
        changeAll(forLastUpdates, b -> b.setLastVerseNo(b.getLastVerseNo() + 1));

        undoRedoOp.redo(() -> {
            changeAll(forFirstUpdates, b -> b.setFirstVerseNo(b.getFirstVerseNo() + 1));
            changeAll(forLastUpdates, b -> b.setLastVerseNo(b.getLastVerseNo() + 1));
        }).undo(() -> {
            changeAll(forFirstUpdates, b -> b.setFirstVerseNo(b.getFirstVerseNo() - 1));
            changeAll(forLastUpdates, b -> b.setLastVerseNo(b.getLastVerseNo() - 1));
        });
    }

    public void verseRemoved(Verse verse, Operation undoRedoOp) {
        List<Block> blocks = blockRepository.getByPageLayer(verse.getPageLayer());
        long vNo = verse.getVerseNo();
        List<Block> forRemoves = blocks.stream()
                .filter(b -> b.getFirstVerseNo() == vNo && b.getLastVerseNo() == vNo)
                .collect(toList());
        List<Block> forFirstUpdates = blocks.stream().filter(b -> b.getFirstVerseNo() > vNo).collect(toList());
        List<Block> forLastUpdates = blocks.stream()
                .filter(b -> b.getLastVerseNo() >= vNo && !forRemoves.contains(b)).collect(toList());
        blockRepository.deleteAll(forRemoves);
        changeAll(forFirstUpdates, b -> b.setFirstVerseNo(b.getFirstVerseNo() - 1));
        changeAll(forLastUpdates, b -> b.setLastVerseNo(b.getLastVerseNo() - 1));

        undoRedoOp.redo(() -> {
            blockRepository.deleteAll(forRemoves);
            changeAll(forFirstUpdates, b -> b.setFirstVerseNo(b.getFirstVerseNo() - 1));
            changeAll(forLastUpdates, b -> b.setLastVerseNo(b.getLastVerseNo() - 1));
        }).undo(() -> {
            blockRepository.saveAll(forRemoves);
            changeAll(forFirstUpdates, b -> b.setFirstVerseNo(b.getFirstVerseNo() + 1));
            changeAll(forLastUpdates, b -> b.setLastVerseNo(b.getLastVerseNo() + 1));
        });
    }

    private void changeAll(Collection<Block> blocks, Consumer<Block> change) {
        blocks.forEach(change);
        blockRepository.saveAll(blocks);
    }

    private List<BlockDto> getStructure(List<BlockDto> blocks) {
        blocks.sort((b1, b2) -> {
            int result = Integer.compare(b1.getFirstVerseNo(), b2.getFirstVerseNo());
            if (result == 0)
                result = -Integer.compare(b1.getLastVerseNo(), b2.getLastVerseNo());
            if (result == 0)
                result = Long.compare(b1.getBlockId() != null ? b1.getBlockId() : Long.MAX_VALUE,
                        b2.getBlockId() != null ? b2.getBlockId() : Long.MAX_VALUE);
            return result;
        });
        List<BlockDto> structure = new ArrayList<>();
        ArrayDeque<BlockDto> dfs = new ArrayDeque<>(blocks);

        while (!dfs.isEmpty()) {
            structure.add(takeBlockStructure(dfs));
        }

        return structure;
    }

    private BlockDto takeBlockStructure(ArrayDeque<BlockDto> dfs) {
        BlockDto head = dfs.remove();
        var subBlocks = head.getChildren();
        while (!dfs.isEmpty() && dfs.peek().getFirstVerseNo() <= head.getLastVerseNo()) {
            BlockDto sub = takeBlockStructure(dfs);
            if (sub.getLastVerseNo() > head.getLastVerseNo()) {
                throw new IllegalArgumentException("Block [" + sub.getFirstVerseNo() + "-" + sub.getLastVerseNo()
                        + "] conflicts with block [" + head.getFirstVerseNo() + "-" + head.getLastVerseNo() + "]");
            }
            subBlocks.add(sub);
        }
        return head;
    }

    private List<Object> getFullStructure(PageLayer pageLayer, List<Block> blocks, List<Verse> verses) {
        List<Object> structure = new ArrayList<>(getStructure(blocks.stream().map(BlockDto::from).collect(toList())));
        fillVerses(structure, verses);
        checkConsistency(null, structure);
        checkPotentialContinuation(pageLayer, structure);
        return structure;
    }

    private void fillVerses(List<Object> structure, List<Verse> verses) {
        verses.sort(Comparator.comparing(Verse::getVerseNo));
        ArrayDeque<Verse> queue = new ArrayDeque<>(verses);
        fillVersesRec(structure, queue);
        queue.forEach(structure::add);
    }

    private void fillVersesRec(List<Object> structure, ArrayDeque<Verse> queue) {
        ArrayList<Object> result = new ArrayList<>();
        for (Object item : structure) {
            if (item instanceof BlockDto) {
                BlockDto block = (BlockDto) item;
                while (!queue.isEmpty() && queue.peek().getVerseNo() < block.getFirstVerseNo())
                    result.add(queue.remove());
                List<Object> children = block.getChildren();
                fillVersesRec(children, queue);
                while (!queue.isEmpty() && queue.peek().getVerseNo() <= block.getLastVerseNo())
                    children.add(queue.remove());
            }
            result.add(item);
        }
        structure.clear();
        structure.addAll(result);
    }

    private void checkConsistency(BlockDto parent, List<Object> children) {
        BlockType pType = parent != null ? parent.getType() : null;
        long verseCount = children.stream().filter(c -> c instanceof Verse).count();

        if (pType == BlockType.IMAGE && verseCount != 1) {
            parent.addConsistencyError(BlockConsistencyError.ONE_VERSE_ALLOWED);
        } else if (pType != null && BlockType.FORCED_RELATIONS.containsKey(pType) && verseCount > 0) {
            parent.addConsistencyError(BlockConsistencyError.VERSES_NOT_ALLOWED);
        }

        BlockType allowedCType = pType == null ? null : BlockType.FORCED_RELATIONS.get(pType);

        for (Object child : children) {
            if (child instanceof BlockDto) {
                BlockDto cBlock = (BlockDto) child;
                BlockType cType = cBlock.getType();

                if (allowedCType != null) {
                    if (cType != allowedCType)
                        cBlock.addConsistencyError(BlockConsistencyError.INVALID_PARENT);
                } else if (BlockType.FORCED_RELATIONS.containsValue(cType)) {
                    cBlock.addConsistencyError(parent == null ? BlockConsistencyError.ROOT_NOT_ALLOWED
                            : BlockConsistencyError.INVALID_PARENT);
                }
                checkConsistency((BlockDto) child, ((BlockDto) child).getChildren());
            }
        }
    }

    private void checkPotentialContinuation(PageLayer pageLayer, List<? extends Object> versesAndBlocks) {
        List<BlockDto> roots = onlyBlocks(versesAndBlocks);
        if (roots.isEmpty())
            return;
        Page page = pageLayer.getPage();
        int pageNo = page.getPageNo();
        if (pageNo == 1) {
            roots.stream().filter(BlockDto::isContinuation)
                    .forEach(b -> b.addConsistencyError(BlockConsistencyError.CONTINUATION_INCONSISTENCY));
        } else {
            Page prevPage = pageRepository.findByTranscriptionIdAndPageNo(page.getTranscription().getId(), pageNo - 1);
            List<Block> prevBlocks = blockRepository.getByPageLayerId(
                    new PageLayer.Id(prevPage.getId(), pageLayer.getLayer().getId()));
            List<BlockDto> prevRoots = getStructure(prevBlocks.stream().map(BlockDto::from).collect(toList()));
            checkPotentialContinuationRec(roots, prevRoots);
        }

        Page nextPage = pageRepository.findByTranscriptionIdAndPageNo(page.getTranscription().getId(), pageNo + 1);
        if (nextPage == null)
            return;
        List<Block> nextContBlocks = blockRepository.getByPageLayerIdAndContinuation(
                new PageLayer.Id(nextPage.getId(), pageLayer.getLayer().getId()), true);
        List<BlockDto> nextContRoots = getStructure(nextContBlocks.stream().map(BlockDto::from).collect(toList()));
        checkContinuedRec(roots, nextContRoots);
    }

    private void checkPotentialContinuationRec(List<BlockDto> blocks, List<BlockDto> prevBlocks) {
        BlockDto prevFirstCont = prevBlocks.stream().filter(BlockDto::isContinuation).findFirst().orElse(null);
        int prevToSkip = prevFirstCont == null ? 0 : prevBlocks.indexOf(prevFirstCont);
        List<BlockType> typesToContinue = prevBlocks.stream().skip(prevToSkip).map(BlockDto::getType)
                .distinct().collect(toList());

        BlockDto firstCont = blocks.stream().filter(BlockDto::isContinuation).findFirst().orElse(null);
        if (firstCont == null) {
            blocks.forEach(b -> {
                b.setPotentialContinuation(typesToContinue.contains(b.getType()));
                checkPotentialContinuationRec(onlyBlocks(b.getChildren()), List.of());
            });
        } else {
            if (typesToContinue.contains(firstCont.getType())) {
                firstCont.setPotentialContinuation(true);
                BlockDto matchingPrev = prevBlocks.stream()
                        .skip(prevToSkip)
                        .filter(b -> b.getType() == firstCont.getType())
                        .reduce((b1, b2) -> b2).orElseThrow();
                checkPotentialContinuationRec(onlyBlocks(firstCont.getChildren()),
                        onlyBlocks(matchingPrev.getChildren()));
            } else {
                firstCont.addConsistencyError(BlockConsistencyError.CONTINUATION_INCONSISTENCY);
            }
            blocks.stream().skip(blocks.indexOf(firstCont) + 1L)
                    .filter(BlockDto::isContinuation)
                    .forEach(b -> b.addConsistencyError(BlockConsistencyError.CONTINUATION_INCONSISTENCY));
        }
    }

    private void checkContinuedRec(List<BlockDto> blocks, List<BlockDto> nextContBlocks) {
        if (nextContBlocks.isEmpty())
            return;
        BlockDto contNextBlock = nextContBlocks.get(0);
        BlockType continuedType = contNextBlock.getType();
        blocks.stream().filter(b -> b.getType() == continuedType)
                .reduce((b1, b2) -> b2)
                .ifPresent(b -> {
                    b.setContinued(true);
                    checkContinuedRec(onlyBlocks(b.getChildren()), onlyBlocks(contNextBlock.getChildren()));
                });
    }
}
