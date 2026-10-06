package pl.psnc.vtl.portal.services.pages.content;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.dto.blocks.BlockDto;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.services.blocks.BlockService;
import pl.psnc.vtl.portal.services.exports.mappers.PlainTextMapper;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Generates page content in plain text format
 */
@Service
@RequiredArgsConstructor
public class PlainTextTranscriptionPageContentGenerator implements TranscriptionPageContentGenerator {

    private final BlockService blockService;

    @Override
    public boolean supports(TranscriptionPageContentType transcriptionPageContentType) {
        return transcriptionPageContentType == TranscriptionPageContentType.PLAIN_TEXT;
    }

    @Override
    public List<ImmutablePair<Layer, String>> generate(Page page, List<Layer> layers) {
        return layers.stream().map(layer -> {
            StringBuilder pageTextContent = new StringBuilder();
            List<BlockDto> blocks = blockService.getBlocksForExport(page, layer);
            for (BlockDto block : blocks) {
                PlainTextMapper.toText(block, pageTextContent);
            }
            return new ImmutablePair<>(layer, StringEscapeUtils.unescapeHtml4(pageTextContent.toString()));
        }).collect(Collectors.toList());
    }

}
