package pl.psnc.vtl.portal.services.pages.content;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.dto.blocks.BlockDto;
import pl.psnc.vtl.portal.model.blocks.BlockType;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.blocks.BlockService;
import pl.psnc.vtl.portal.services.exports.mappers.HOCRMapper;
import pl.psnc.vtl.portal.services.exports.utils.XmlPrintingUtil;
import pl.psnc.vtl.portal.services.imports.BeanLoader;
import pl.psnc.vtl.portal.services.verses.VerseService;

import java.util.EnumSet;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class HOCRTranscriptionPageContentGenerator implements TranscriptionPageContentGenerator {

    private static final EnumSet<BlockType> INLINE_BLOCKS = EnumSet.of(
            BlockType.ADD, BlockType.ADD_ABOVE, BlockType.ADD_BELOW, BlockType.ADD_MARGIN, BlockType.DEL,
            BlockType.SUBST);

    private final BlockService blockService;

    @Value("${vtl.iiif-server-pct}")
    private Float pdfScaleFactor;

    @Override
    public boolean supports(TranscriptionPageContentType transcriptionPageContentType) {
        return transcriptionPageContentType == TranscriptionPageContentType.HOCR;
    }

    @Override
    public List<ImmutablePair<Layer, String>> generate(Page page, List<Layer> layers) {
        prefetchVerses(page);
        return layers.stream().map(layer -> {
            StringBuilder result = new StringBuilder();
            result.append(HOCRMapper.getHead());
            result.append("<body>\n");
            getHOCRBody(page, false, layer, result);
            result.append("</body>\n</html>");
            return new ImmutablePair<>(layer, XmlPrintingUtil.prettyPrintHocr(result.toString()));
        }).collect(toList());
    }

    public String generateForPdf(Page page, Layer layer) {
        prefetchVerses(page);
        StringBuilder result = new StringBuilder();
        result.append(HOCRMapper.getHead());
        result.append("<body>\n");
        getHOCRBody(page, true, layer, result);
        result.append("</body>\n</html>");
        return XmlPrintingUtil.prettyPrintHocr(result.toString());
    }

    private void prefetchVerses(Page page) {
        List<Verse> verses = BeanLoader.getBean(VerseService.class).completeVerses(page);
        page.setVerses(verses);

        page.getPageLayers().forEach(pageLayer -> pageLayer.setVerses(
                verses.stream().filter(v -> v.getPageLayer().getId().equals(pageLayer.getId())).collect(toList())));
    }

    private void getHOCRBody(Page page, boolean isForPdf, Layer layer, StringBuilder result) {
        HOCRMapper hocrMapper = new HOCRMapper(pdfScaleFactor);
        hocrMapper.setPdfMode(isForPdf);

        String htmlOcrPage = hocrMapper.getOcrPage(page);
        result.append(htmlOcrPage);

        List<BlockDto> blocks = blockService.getBlocksForExport(page, layer);
        if (isForPdf) {
            removeImageVerses(blocks);
        }
        blocks.forEach(b -> inlineBlocks(b.getChildren()));
        for (BlockDto block : blocks) {
            result.append(hocrMapper.mapToHocr(page, block));
        }

        result.append("</div>\n");
    }

    private void removeImageVerses(List<? extends Object> items) {
        for (var it = items.listIterator(); it.hasNext();) {
            Object item = it.next();
            if (item instanceof Verse) {
                if (((Verse) item).isImage())
                    it.remove();
            } else {
                removeImageVerses(((BlockDto) item).getChildren());
            }
        }
    }

    private void inlineBlocks(List<Object> items) {
        for (var it = items.listIterator(); it.hasNext();) {
            Object item = it.next();
            if (!(item instanceof BlockDto))
                continue;
            BlockDto block = (BlockDto) item;
            if (INLINE_BLOCKS.contains(block.getType())) {
                it.remove();
                block.getChildren().forEach(it::add);
            }
        }
        for (Object item : items) {
            if (item instanceof BlockDto)
                inlineBlocks(((BlockDto) item).getChildren());
        }
    }

    public String getHOCRBody(Page page, Layer layer) {
        StringBuilder sb = new StringBuilder();
        getHOCRBody(page, false, layer, sb);
        return sb.toString();
    }

}
