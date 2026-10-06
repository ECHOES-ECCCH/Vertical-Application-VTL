package pl.psnc.vtl.portal.services.imports.dace;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.*;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class DaceMapperStrategy implements MapperStrategy, LinkMapperStrategy {
    private final MappingConfiguration mappingConfiguration;

    public DaceMapperStrategy(MappingConfiguration mappingConfiguration) {
        this.mappingConfiguration = mappingConfiguration;
    }

    @Override
    public TranscriptionCore mapToImportTemplate(String content) {
        TranscriptionCore core = new TranscriptionCore();
        DaceJsonHandler daceJsonHandler = new DaceJsonHandler(content);
        String daceId = daceJsonHandler.getDaceId();
        if (Strings.isNullOrEmpty(daceId)) {
            throw new IllegalArgumentException("No external identifier provided, cannot import dace record!");
        }
        core.setExternalId(daceId);
        String recordBody = daceJsonHandler.getRecordBody();

        DaceRecordBodyHandler daceRecordBodyHandler = new DaceRecordBodyHandler(recordBody);
        daceRecordBodyHandler.getTitle().ifPresent(core.getAnalyticInfo()::setTitle);
        daceRecordBodyHandler.getDescriptions().forEach(summary -> {
            if (!Strings.isNullOrEmpty(summary)) {
                core.getProfileDesc().setSummary(summary);
                core.getTranscriptionInfo().setDescription(summary);
            }
        });
        List<String> keywords = daceRecordBodyHandler.getKeywords();
        if (!keywords.isEmpty()) {
            core.getProfileDesc().setKeywords(keywords);
        }
        daceRecordBodyHandler.getDate().ifPresent(core.getMonographicInfo()::setPublicationDate);
        daceRecordBodyHandler.getTypes().stream().filter(s -> !Strings.isNullOrEmpty(s)).forEach(type -> {
            if (!core.isAnalytic()) {
                core.setAnalytic(mappingConfiguration.getAnalyticCharacteristics().contains(type));
            }
        });
        return core;
    }

    @Override
    public PublicationLinks getTranscriptionLinks(LoadedFile loadedFile) {
        DaceRecordBodyHandler daceRecordBodyHandler = new DaceRecordBodyHandler(
                new DaceJsonHandler(loadedFile.getFileContent()).getRecordBody());
        List<String> links = daceRecordBodyHandler.getContentUrls().stream().filter(s -> !Strings.isNullOrEmpty(s))
                .reduce(new ArrayList<>(),
                        new ContentUrlsAccumulator(this::determineContentType, this::determineContentType),
                        new ContentUrlsCombiner(this::determineContentType));
        Function<List<Link>, PublicationLinks> handler = LinkMappingConfiguration.getHandler(
                determineContentType(links));
        return handler.apply(links.stream().map(Link::new).collect(Collectors.toList()));
    }

    private String determineContentType(List<String> links) {
        return determineContentType(links.isEmpty() ? null : links.get(0));
    }

    private String determineContentType(String link) {
        if (link != null) {
            String extension = FilenameUtils.getExtension(link);
            if (LinkMappingConfiguration.IFF_EXTENSION.equals(extension) ||
                    LinkMappingConfiguration.DJBZ_EXTENSION.equals(extension) ||
                    LinkMappingConfiguration.THUMB_EXTENSION.equals(extension)) {
                return LinkMappingConfiguration.IMAGE_X_DJVU_MIMETYPE;
            }
        }
        Optional<MediaType> mediaType = Optional.empty();
        try {
            mediaType = MediaTypeFactory.getMediaType(link == null ? null : new UrlResource(link));
        } catch (MalformedURLException e) {
            log.error("Could not determine media type from a link {}", link, e);
        }
        return mediaType.orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
    }
}
