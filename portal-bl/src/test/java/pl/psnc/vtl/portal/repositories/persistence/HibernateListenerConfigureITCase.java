package pl.psnc.vtl.portal.repositories.persistence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.AnalyticInfo;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;

import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class HibernateListenerConfigureITCase {

    @Autowired
    TranscriptionRepository transcriptionRepository;

    @Autowired
    VerseRepository verseRepository;

    @Autowired
    PageRepository pageRepository;

    private final long transcriptionId = 1L;

    private final long pageId = 1L;

    @Autowired
    private MockMvc mvc;

    private String TOKEN;

    private String TOKEN_2;


    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, "email@wp.pl", "myPassword");
        TOKEN_2 = LogInService.getJWTToken(mvc, "email2@wp.pl", "myPassword");

    }


    @Test
    public void shouldChangeVersionWhenVersesUpdated() {
        LocalDateTime modificationDateTime = transcriptionRepository.getById(transcriptionId).getLastModificationDate();
        Verse verse = verseRepository.findByPageTranscriptionId(transcriptionId).get(0);
        verse.setContent("newContent");
        verseRepository.saveAndFlush(verse);
        LocalDateTime lastModificationDate = transcriptionRepository.getById(transcriptionId).getLastModificationDate();
        assertNotEquals(modificationDateTime, lastModificationDate);
    }


    @Test
    public void shouldChangeVersionWhenPagesUpdated() {
        LocalDateTime modificationDateTime = transcriptionRepository.getOne(transcriptionId).getLastModificationDate();
        Page page = pageRepository.findByTranscriptionId(transcriptionId).get(0);
        page.setPath("newPath");
        pageRepository.saveAndFlush(page);
        LocalDateTime lastModificationDate = transcriptionRepository.getOne(transcriptionId).getLastModificationDate();
        assertNotEquals(modificationDateTime, lastModificationDate);
    }


    @Test
    public void shouldChangeVersionWhenMetadataChanged() {
        Transcription transcription = transcriptionRepository.getOne(transcriptionId);
        LocalDateTime modificationDateTime = transcription.getLastModificationDate();
        AnalyticInfo analyticInfo = transcription.getAnalyticInfo();
        analyticInfo.setTitle("newTitle");
        transcription.setAnalyticInfo(new AnalyticInfo());
        transcriptionRepository.saveAndFlush(transcription);
        LocalDateTime lastModificationDate = transcriptionRepository.getOne(transcriptionId).getLastModificationDate();
        assertNotEquals(modificationDateTime, lastModificationDate);
    }


    @Test
    public void shouldChangePageVersionWhenVersesUpdated() {
        LocalDateTime modificationDateTime = pageRepository.getById(pageId).getLastModificationDate();
        Verse verse = verseRepository.findByPageTranscriptionId(pageId).get(0);
        verse.setContent("newContent");
        verseRepository.saveAndFlush(verse);
        LocalDateTime lastModificationDate = pageRepository.getById(pageId).getLastModificationDate();
        assertNotEquals(modificationDateTime, lastModificationDate);
    }


    @Test
    public void shouldChangePageVersionWhenPageDataUpdated() {
        LocalDateTime modificationDateTime = pageRepository.getOne(pageId).getLastModificationDate();

        Page page = pageRepository.getOne(pageId);
        page.setPath("page0004.tif");
        pageRepository.saveAndFlush(page);
        page.setPath("page0004r.tif");
        pageRepository.saveAndFlush(page);

        LocalDateTime lastModificationDate = pageRepository.getOne(pageId).getLastModificationDate();
        assertNotEquals(modificationDateTime, lastModificationDate);
    }


    @Test
    public void shouldChangeLastModifierWhenVerseDataUpdated()
            throws Exception {

        mvc.perform(delete("/api/verses/{verseId}", verseRepository.findById(1L).get().getId()).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        String lastModifier = pageRepository.getOne(pageId).getLastModifier();
        assertNotNull(lastModifier);

        mvc.perform(delete("/api/verses/{verseId}", verseRepository.findById(2L).get().getId()).header("Authorization", TOKEN_2)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        String currentModifier = pageRepository.getOne(pageId).getLastModifier();
        assertNotNull(currentModifier);
        assertNotEquals(lastModifier, currentModifier);
    }

}