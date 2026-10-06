package pl.psnc.vtl.portal.services.pages;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class HOCRPageRecognizer implements ImportPageRecognizer {

    private final PageRepository repository;

    private static final Pattern fileRelativePattern = Pattern.compile("(?i)(.*)_?Page([0-9]+).*");

    @Override
    public boolean supports(TranscriptionPageContentType transcriptionPageContentType) {
        return transcriptionPageContentType == TranscriptionPageContentType.HOCR;
    }

    @Override
    public Optional<Page> recognizePage(long transcriptionId, String hocrContent, String fileName){
        return getPageNoFromHOCRBody(hocrContent)
                .map(pageNo -> repository.findByTranscriptionIdAndPageNo(transcriptionId, pageNo))
                .or(() -> getPageByFileName(transcriptionId, fileName));
    }

    Optional<Page> getPageByFileName(long transcriptionId, String fileName){
        var pair = getPagePathAndRelativeNo(fileName);
        return repository
                .findByTranscriptionIdAndRelativeNoAndPathStartsWithIgnoreCase(
                        transcriptionId, pair.getRight(), pair.getLeft() + ".")
                .or(() -> repository.findByTranscriptionIdAndRelativeNoAndPathStartsWithIgnoreCase(
                        transcriptionId, pair.getRight(), pair.getLeft().replaceFirst("(.*)_.*", "$1") + "."));
    }

    Optional<Integer> getPageNoFromHOCRBody(String hocr) {
        return Jsoup.parse(hocr).getElementsByClass("ocr_page").stream()
                .findFirst()
                .map(Element::attributes)
                .map(attributes -> attributes.get("title"))
                .map(this::getPPageno);
    }

    private Integer getPPageno(String hocrContent) {
        if(!hocrContent.contains("ppageno ")) return null;
        String expresion = "ppageno ";
        int startIndex = hocrContent.indexOf(expresion)+expresion.length();
        return Integer.parseInt(hocrContent.substring(startIndex));
    }

    ImmutablePair<String, Integer> getPagePathAndRelativeNo(String fileName){
        Matcher matcher = fileRelativePattern.matcher(fileName);
        if (matcher.find()) {
            return new ImmutablePair<>(matcher.group(1), Integer.parseInt(matcher.group(2)));
        } else {
            return new ImmutablePair<>(FilenameUtils.getBaseName(fileName), null);
        }
    }

}
