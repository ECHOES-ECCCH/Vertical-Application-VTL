package pl.psnc.vtl.portal.services.parsers.plain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.verses.VerseStatus;
import pl.psnc.vtl.portal.services.parsers.TranscriptionContentParser;
import pl.psnc.vtl.portal.services.parsers.ocr.OCRToolsService;
import pl.psnc.vtl.portal.services.words.WordService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PlainTextParser implements TranscriptionContentParser {

    private final OCRToolsService ocrToolsService;
    private final WordService wordService;

    @Override
    public boolean support(TranscriptionPageContentType pageContentType) {
        return TranscriptionPageContentType.PLAIN_TEXT == pageContentType;
    }


    private Deque<String> readFile(InputStream inputStream) throws IOException {
        Deque<String> result = new ArrayDeque<>();
        try (inputStream; Scanner sc = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            while (sc.hasNextLine()) {
                result.add(sc.nextLine());
            }
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        }

        return result;
    }

    private String getTranscriptionLanguage(Transcription transcription) {
        String transcriptionLanguage = "pol";
        if (transcription.getAnalyticInfo() != null && transcription.getAnalyticInfo().getLanguage() != null) {
            transcriptionLanguage = transcription.getAnalyticInfo().getLanguage().getCode();
        }
        return transcriptionLanguage;
    }

    @Override
    public List<List<Verse>> parseVerses(InputStream inputStream, List<Page> pages) throws IOException {
        Deque<String> lines = readFile(inputStream);

        Transcription transcription = pages.get(0).getTranscription();
        String transcriptionLanguage = getTranscriptionLanguage(transcription);
        int pageNo = 0;
        List<List<Verse>> result = new ArrayList<>();

        while (!lines.isEmpty() && pages.size() > pageNo) {
            List<Verse> verses = ocrToolsService.recognizePage(pages.get(pageNo),
                    transcriptionLanguage, true, false);
            int i = 0;
            while (!lines.isEmpty() && verses.size() > i) {
                verses.get(i).setContent(lines.removeFirst());
                wordService.synchronizeWords(verses.get(i));
                verses.get(i).setStatus(VerseStatus.NONE);
                verses.get(i).setFromOCR(false);
                verses.get(i).setLang(null);
                verses.get(i).setFormatting(new Formatting());
                i++;
            }

            result.add(verses.subList(0, i));
            pageNo++;
        }
        
        return result;
    }
}
