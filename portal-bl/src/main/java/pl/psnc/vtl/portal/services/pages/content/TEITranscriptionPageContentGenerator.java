package pl.psnc.vtl.portal.services.pages.content;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.dto.blocks.BlockDto;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.services.blocks.BlockService;
import pl.psnc.vtl.portal.services.exports.tei.TeiDocumentComposer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class TEITranscriptionPageContentGenerator implements TranscriptionPageContentGenerator {

    private final BlockService blockService;

    @Override
    public boolean supports(TranscriptionPageContentType transcriptionPageContentType) {
        return transcriptionPageContentType == TranscriptionPageContentType.TEI;
    }

    @Override
    public List<ImmutablePair<Layer, String>> generate(@NonNull Page page, List<Layer> layers) throws IOException {
        List<ImmutablePair<Layer, String>> result = new ArrayList<>();
        for (Layer layer : layers) {
            List<BlockDto> blocks = blockService.getBlocksForExport(page, layer);
            String content = TeiDocumentComposer.composeForPage(page, blocks);
            result.add(new ImmutablePair<>(layer, content));
        }
        return result;

    }
}
