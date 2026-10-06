package pl.psnc.vtl.portal.services.transcriptions.content;

import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.dto.blocks.BlockDto;
import pl.psnc.vtl.portal.model.blocks.BlockType;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.services.blocks.BlockService;
import pl.psnc.vtl.portal.services.exports.mappers.PlainTextMapper;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Service
@AllArgsConstructor
public class PlainTexTranscriptionContentGenerator implements TranscriptionContentGenerator {

    private static final EnumSet<BlockType> INLINE_BLOCKS = EnumSet.of(
            BlockType.ADD, BlockType.ADD_ABOVE, BlockType.ADD_BELOW, BlockType.ADD_MARGIN, BlockType.SUBST);
    private static final EnumSet<BlockType> SKIP_BLOCKS = EnumSet.of(BlockType.DEL);

    private final PageRepository pageRepository;
    private final BlockService blockService;

    @Override
    public boolean supports(TranscriptionPageContentType transcriptionPageContentType) {
        return TranscriptionPageContentType.PLAIN_TEXT == transcriptionPageContentType;
    }

    @Override
    public String generate(Transcription transcription, Layer layer) {
        List<Page> pages = pageRepository.findByTranscriptionIdOrderByPageNo(transcription.getId());
        StringBuilder result = new StringBuilder();
        List<BlockDto> annotations = new ArrayList<>();
        for (Page page : pages) {
            List<BlockDto> blocks = blockService.getBlocksForExport(page, layer);
            blocks.forEach(b -> inlineAndSkipBlocks(b.getChildren()));
            putAnnotationsAside(blocks, annotations);
            for (BlockDto block : blocks) {
                PlainTextMapper.toText(block, result);
            }
        }

        if (CollectionUtils.isNotEmpty(annotations)) {
            while (Character.isWhitespace(result.charAt(result.length() - 1)))
                result.deleteCharAt(result.length() - 1);
            result.append("\n\n------\n");
            for (BlockDto annotation : annotations)
                PlainTextMapper.toText(annotation, result);
        }
        return StringEscapeUtils.unescapeHtml4(result.toString());
    }

    private void inlineAndSkipBlocks(List<Object> items) {
        for (var it = items.listIterator(); it.hasNext();) {
            Object item = it.next();
            if (!(item instanceof BlockDto))
                continue;
            BlockDto block = (BlockDto) item;
            if (INLINE_BLOCKS.contains(block.getType())) {
                it.remove();
                block.getChildren().forEach(it::add);
            }
            if (SKIP_BLOCKS.contains(block.getType())) {
                it.remove();
            }
        }
        for (Object item : items) {
            if (item instanceof BlockDto)
                inlineAndSkipBlocks(((BlockDto) item).getChildren());
        }
    }

    private void putAnnotationsAside(List<? extends Object> blocks, List<BlockDto> annotations) {
        for (var it = blocks.iterator(); it.hasNext();) {
            Object item = it.next();
            if (!(item instanceof BlockDto))
                continue;
            BlockDto block = (BlockDto) item;
            if (block.getType() == BlockType.ANNOTATION) {
                it.remove();
                annotations.add(block);
            } else {
                putAnnotationsAside(block.getChildren(), annotations);
            }
        }
    }
}
