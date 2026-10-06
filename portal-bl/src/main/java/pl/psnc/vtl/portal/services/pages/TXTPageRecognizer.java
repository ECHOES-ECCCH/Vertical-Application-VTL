package pl.psnc.vtl.portal.services.pages;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TXTPageRecognizer implements ImportPageRecognizer {

    private final PageRepository repository;

    private static final Pattern fileRelativePattern = Pattern.compile("(?i)(.*)_?Page([0-9]+).*");

    @Override
    public boolean supports(TranscriptionPageContentType transcriptionPageContentType) {
        return transcriptionPageContentType == TranscriptionPageContentType.PLAIN_TEXT;
    }

    @Override
    public Optional<Page> recognizePage(long transcriptionId, String hocrContent, String fileName) {
        var pair = getPagePathAndRelativeNo(fileName);
        return repository
                .findByTranscriptionIdAndRelativeNoAndPathStartsWithIgnoreCase(
                        transcriptionId, pair.getRight(), pair.getLeft() + ".")
                .or(() -> repository.findByTranscriptionIdAndRelativeNoAndPathStartsWithIgnoreCase(
                        transcriptionId, pair.getRight(), pair.getLeft().replaceFirst("(.*)_.*", "$1") + "."));
    }

    ImmutablePair<String, Integer> getPagePathAndRelativeNo(String fileName) {
        Matcher matcher = fileRelativePattern.matcher(fileName);
        if (matcher.find()) {
            return new ImmutablePair<>(matcher.group(1), Integer.parseInt(matcher.group(2)));
        } else {
            return new ImmutablePair<>(FilenameUtils.getBaseName(fileName), null);
        }
    }
}
