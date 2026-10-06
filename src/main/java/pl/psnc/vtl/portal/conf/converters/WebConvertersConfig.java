package pl.psnc.vtl.portal.conf.converters;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.dto.blocks.MultipageBlockRemoveMode;
import pl.psnc.vtl.portal.dto.transcriptions.*;
import pl.psnc.vtl.portal.model.imports.FileType;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.model.verses.*;
import pl.psnc.vtl.portal.repositories.verses.StatsSortField;
import pl.psnc.vtl.portal.services.eBook.Ebook;
import pl.psnc.vtl.portal.services.events.EventService.EventCategory;
import pl.psnc.vtl.portal.services.parsers.ocr.OcrLevel;

@Configuration
public class WebConvertersConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldType(VerseStatus.class, new EnumFormatter<>(VerseStatus.class));
        registry.addFormatterForFieldType(Heading.class, new EnumFormatter<>(Heading.class));
        registry.addFormatterForFieldType(FormeWork.class, new EnumFormatter<>(FormeWork.class));
        registry.addFormatterForFieldType(TabularForm.class, new EnumFormatter<>(TabularForm.class));
        registry.addFormatterForFieldType(ListForm.class, new EnumFormatter<>(ListForm.class));
        registry.addFormatterForFieldType(SearchOrder.class, new EnumFormatter<>(SearchOrder.class));
        registry.addFormatterForFieldType(SearchScope.class, new EnumFormatter<>(SearchScope.class));
        registry.addFormatterForFieldType(SearchType.class, new EnumFormatter<>(SearchType.class));
        registry.addFormatterForFieldType(SearchVisibility.class, new EnumFormatter<>(SearchVisibility.class));
        registry.addFormatterForFieldType(MultipageBlockRemoveMode.class,
                new EnumFormatter<>(MultipageBlockRemoveMode.class));
        registry.addFormatterForFieldType(TranscriptionContinuation.State.class,
                new EnumFormatter<>(TranscriptionContinuation.State.class));
        registry.addFormatterForFieldType(FileType.class, new EnumFormatter<>(FileType.class));
        registry.addFormatterForFieldType(StaticFileLanguage.class, new EnumFormatter<>(StaticFileLanguage.class));
        registry.addFormatterForFieldType(OcrMode.class, new EnumFormatter<>(OcrMode.class));
        registry.addFormatterForFieldType(OcrLevel.class, new EnumFormatter<>(OcrLevel.class));
        registry.addFormatterForFieldType(SecurityGroup.class, new EnumFormatter<>(SecurityGroup.class));
        registry.addFormatterForFieldType(TranscriptionPageContentType.class, new EnumFormatter<>(TranscriptionPageContentType.class));
        registry.addFormatterForFieldType(Ebook.class, new EnumFormatter<>(Ebook.class));
        registry.addFormatterForFieldType(EventCategory.class, new EnumFormatter<>(EventCategory.class));
        registry.addFormatterForFieldType(
                StatsSortField.class, new EnumFormatter<>(StatsSortField.class));
        registry.addFormatterForFieldType(Sort.Direction.class, new EnumFormatter<>(Sort.Direction.class));
    }

}
