package pl.psnc.vtl.portal.services.parsers.ocr;

import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.services.parsers.Converter;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;
import pl.psnc.vtl.portal.services.parsers.ocr.converters.*;

@Component
public class HocrConverterFactory extends ConverterFactory {

    public HocrConverterFactory() {
        super(TranscriptionPageContentType.HOCR);
    }

    public Converter getConverter(String name) {
        switch (name.toLowerCase()) {
            case Hocr.OCR_PAGENO_CLASS_NAME:
                return new PageNumberConverter(converterSettings);
            case Hocr.OCR_HEADER_CLASS_NAME:
                return new HeaderConverter(converterSettings);
            case Hocr.OCR_FOOTER_CLASS_NAME:
                return new FooterConverter(converterSettings);
            case Hocr.OCR_CAPTION_CLASS_NAME:
                return new CaptionConverter(converterSettings);
            case Hocr.OCR_CAREA_CLASS_NAME:
                return new AreaConverter(converterSettings);
            case Hocr.OCR_PARAGRAPH_CLASS_NAME:
                return new ParagraphConverter(converterSettings);
            case Hocr.OCR_SECTION_CLASS_NAME:
                return new SectionConverter(converterSettings);
            case Hocr.OCR_SUBSECTION_CLASS_NAME:
                return new SubSectionConverter(converterSettings);
            case Hocr.OCR_SUBSUBSECTION_CLASS_NAME:
                return new SubSubSectionConverter(converterSettings);
            case Hocr.OCR_TABLE_CLASS_NAME:
                return new TableConverter(converterSettings);
            case Hocr.OCR_IMAGE_CLASS_NAME:
                return new ImageConverter(converterSettings);

            case Hocr.OCR_ANNOTATION_CLASS_NAME:
                return new AnnotationConverter(converterSettings);
            case Hocr.OCR_BOLD_CLASS_NAME:
                return new BoldConverter(converterSettings);
            case Hocr.OCR_ITALIC_CLASS_NAME:
                return new ItalicConverter(converterSettings);
            case Hocr.OCR_UNDERLINE_CLASS_NAME:
                return new UnderlineConverter(converterSettings);
            case Hocr.OCR_STRIKETHROUGH_CLASS_NAME:
                return new StrikethroughConverter(converterSettings);
            case Hocr.OCR_NOTATED_MUSIC_CLASS_NAME:
                return new NotatedMusicConverter(converterSettings);
            case Hocr.OCR_MARGINALIA_CLASS_NAME:
                return new MarginaliaConverter(converterSettings);

                default:
                return new NoOpConverter(converterSettings);
        }
    }

    @Override
    public Converter getConverter(Element element) {
        String name = element.className();
        return getConverter(name);
    }
}
