package pl.psnc.vtl.portal.services.layers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.conf.logging.LogAction;
import pl.psnc.vtl.portal.dto.layers.LayerCore;
import pl.psnc.vtl.portal.dto.layers.LayerCreationDto;
import pl.psnc.vtl.portal.dto.layers.LayerDto;
import pl.psnc.vtl.portal.model.blocks.Block;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.words.Word;
import pl.psnc.vtl.portal.repositories.blocks.BlockRepository;
import pl.psnc.vtl.portal.repositories.layers.LayerRepository;
import pl.psnc.vtl.portal.repositories.pages.PageLayersRepository;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.repositories.words.WordRepository;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionService;

import javax.transaction.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for creating new layers based on the client requirements (defined in {@link LayerCreationDto}).
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LayerCreationService {

    private final LayerService layerService;

    private final LayerRepository layerRepository;

    private final PageLayerService pageLayerService;

    private final PageLayersRepository pageLayersRepository;

    private final WordRepository wordRepository;

    private final VerseRepository verseRepository;

    private final BlockRepository blockRepository;

    private final PageRepository pageRepository;

    private final TranscriptionPermissionService transcriptionPermissionService;

    private final TranscriptionService transcriptionService;

    public LayerDto createLayer(long transcriptionId, LayerCreationDto layerCreationDto) {
        LogAction.MODIFY_DATA.info(log, "Creating new layer for transcriptionId={}: {}", transcriptionId,
                layerCreationDto);
        transcriptionService.requireTranscription(transcriptionId);

        transcriptionPermissionService.checkAdminAccess(transcriptionId);

        List<Long> pageIds = pageRepository.findByTranscriptionId(transcriptionId).stream()
                .map(pl.psnc.vtl.portal.model.pages.Page::getId).collect(Collectors.toList());

        Layer firstLayerForTranscription = layerRepository.findFirstByTranscriptionIdOrderById(transcriptionId);

        Layer newlyCreatedLayer = layerService.createLayer(
                new LayerCore(transcriptionId,
                        layerCreationDto.getName(),
                        layerCreationDto.getDescription(),
                        layerCreationDto.getType()
                ));

        if (!pageIds.isEmpty()) {
            pageLayerService.createPageLayers(pageIds, newlyCreatedLayer);
        }

        copyVersesFromFirstLayerToCreatedLayerIfRequested(transcriptionId, newlyCreatedLayer, layerCreationDto);
        copyBlocksFromFirstLayerToCreatedLayerIfRequested(firstLayerForTranscription, layerCreationDto, pageIds,
                newlyCreatedLayer);

        log.info("Layer created for transcriptionId={}: {}", transcriptionId, layerCreationDto);
        return LayerMapper.INSTANCE.toDto(newlyCreatedLayer);
    }

    private void copyVersesFromFirstLayerToCreatedLayerIfRequested(long transcriptionId, Layer newlyCreatedLayer,
                                                                   LayerCreationDto layerCreationDto) {
        if (!layerCreationDto.versesCopyRequested()) {
            log.debug("Verses will not be copied");
            return;
        }
        Layer firstLayerForTranscription = layerService.getFirstLayerForTranscription(transcriptionId);
        List<Verse> allVersesFromFirstLayer =
                verseRepository.findAllByPage_Transcription_IdAndPageLayer_Layer(transcriptionId,
                        firstLayerForTranscription);
        List<Word> allWordsFromFirstLayer = wordRepository.findAllByVerseIn(allVersesFromFirstLayer);
        Map<Long, Verse> copiedVersesByOriginalId = new HashMap<>();

        for (Verse verse : allVersesFromFirstLayer) {
            Verse copiedVerse = Verse.builder()
                    .verseNo(verse.getVerseNo())
                    .content(layerCreationDto.versesWithContentCopyRequested() ? verse.getContent() : null)
                    .formatting(verse.getFormatting())
                    .page(verse.getPage())
                    .pageLayer(findPageLayerFor(newlyCreatedLayer, verse.getPage().getId()))
                    .status(verse.getStatus())
                    .authorOfLastStatusModification(verse.getAuthorOfLastStatusModification())
                    .boundingBox(verse.getBoundingBox())
                    .indexes(new HashSet<>(verse.getIndexes()))
                    .build();
            copiedVersesByOriginalId.put(verse.getId(), verseRepository.save(copiedVerse));
        }

        for (Word word : allWordsFromFirstLayer) {
            Verse copiedVerse = copiedVersesByOriginalId.get(word.getVerse().getId());
            if (copiedVerse == null)
                throw new RuntimeException("Missing copy for verse with id " + word.getVerse().getId());
            Word copiedWord = new Word();
            copiedWord.setContent(layerCreationDto.versesWithContentCopyRequested() ? word.getContent() : "");
            copiedWord.setBoundingBox(word.getBoundingBox());
            copiedWord.setOrdering(word.getOrdering());
            copiedWord.setTeiAdd(word.getTeiAdd());
            copiedWord.setTeiSubst(word.getTeiSubst());
            copiedWord.setTeiDel(word.isTeiDel());
            copiedWord.setTeiGap(word.isTeiGap());

            copiedWord.setVerse(copiedVerse);
            copiedWord.setIndexes(new HashSet<>(word.getIndexes()));
            wordRepository.save(copiedWord);
        }

        log.debug("Verses copied");
    }

    private void copyBlocksFromFirstLayerToCreatedLayerIfRequested(Layer firstLayerForTranscription,
                                                                   LayerCreationDto layerCreationDto,
                                                                   List<Long> pageIds, Layer newlyCreatedLayer) {

        if (!layerCreationDto.blocksCopyRequested()) {
            LogAction.MODIFY_DATA.debug(log, "Blocks will not be copied");
            return;
        }

        pageIds.forEach(pageId -> {
            PageLayer pageLayer = pageLayersRepository.getById(new PageLayer.Id(pageId,
                    firstLayerForTranscription.getId()));

            List<Block> blocks = blockRepository.getByPageLayer(pageLayer);

            PageLayer pageLayerForNewBlocks = findPageLayerFor(newlyCreatedLayer, pageLayer.getPage().getId());

            for (Block sourceBlock : blocks) {
                Block newBlock = Block.builder()
                        .firstVerseNo(sourceBlock.getFirstVerseNo())
                        .lastVerseNo(sourceBlock.getLastVerseNo())
                        .type(sourceBlock.getType())
                        .pageLayer(pageLayerForNewBlocks)
                        .build();
                blockRepository.save(newBlock);
            }
        });
        LogAction.MODIFY_DATA.debug(log, "Blocks copied");
    }

    public PageLayer findPageLayerFor(Layer layer, long pageId) {
        for (PageLayer pageLayer : layer.getLayerPages()) {
            if (pageLayer.getPage().getId().equals(pageId)) {
                return pageLayer;
            }
        }
        return null;
    }

}
