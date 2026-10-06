package pl.psnc.vtl.portal.services.parsers.tei;

import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.services.parsers.Converter;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;
import pl.psnc.vtl.portal.services.parsers.tei.converters.*;

@Component
public class TeiConverterFactory extends ConverterFactory {

    public TeiConverterFactory() {
        super(TranscriptionPageContentType.TEI);
    }

    public Converter getConverter(String elementTypeName) {

        switch (elementTypeName.toLowerCase()) {
            case Tei.TEI_PAGENO_TYPE_NAME:
                return new TeiPageNumberConverter(converterSettings);
            case Tei.TEI_HEADER_TYPE_NAME:
                return new TeiHeaderConverter(converterSettings);
            case Tei.TEI_FOOTER_TYPE_NAME:
                return new TeiFooterConverter(converterSettings);
            case Tei.TEI_CAPTION_TYPE_NAME:
                return new TeiCaptionConverter(converterSettings);
            case Tei.TEI_IMAGE_TYPE_NAME:
                return  new TeiImageConverter(converterSettings);
            case Tei.TEI_PARAGRAPH_TYPE_NAME:
                return new TeiParagraphConverter(converterSettings);
            case Tei.TEI_SECTION_TYPE_NAME:
                return new TeiSectionConverter(converterSettings);
            case Tei.TEI_SUBSECTION_TYPE_NAME:
                return new TeiSubSectionConverter(converterSettings);
            case Tei.TEI_SUBSUBSECTION_TYPE_NAME:
                return new TeiSubSubSectionConverter(converterSettings);
            case Tei.TEI_TABLE_TYPE_NAME:
                return new TeiTableConverter(converterSettings);
            case Tei.TEI_ROW_TYPE_NAME:
                return  new TeiRowConverter(converterSettings);
            case Tei.TEI_CELL_TYPE_NAME:
                return new TeiCellConverter(converterSettings);
            case Tei.TEI_LIST_TYPE_NAME:
                return new TeiListConverter(converterSettings);
            case Tei.TEI_ITEM_TYPE_NAME:
                return new TeiItemConverter(converterSettings);

            case Tei.TEI_ANNOTATION_TYPE_NAME:
                return new TeiAnnotationConverter(converterSettings);
            case Tei.TEI_BOLD_TYPE_NAME:
                return new TeiBoldConverter(converterSettings);
            case Tei.TEI_ITALIC_TYPE_NAME:
                return new TeiItalicConverter(converterSettings);
            case Tei.TEI_UNDERLINE_TYPE_NAME:
                return new TeiUnderlineConverter(converterSettings);
            case Tei.TEI_STRIKETHROUGH_TYPE_NAME:
                return new TeiStrikethroughConverter(converterSettings);
            case Tei.TEI_NOTATED_MUSIC_TYPE_NAME:
                return new TeiNotatedMusicConverter(converterSettings);
            case Tei.TEI_MARGINALIA_TYPE_NAME:
                return new TeiMarginaliaConverter(converterSettings);

            default:
                return new TeiNoOpConverter(converterSettings);
        }
    }

    @Override
    public Converter getConverter(Element element) {
        String name = element.attributes().get("type");
        return getConverter(name);
    }
}
