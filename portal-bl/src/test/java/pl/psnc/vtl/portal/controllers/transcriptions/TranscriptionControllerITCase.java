package pl.psnc.vtl.portal.controllers.transcriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.ResourceFileContentLoader;
import pl.psnc.vtl.portal.conf.TestJsonMapper;
import pl.psnc.vtl.portal.dto.indexes.IndexCore;
import pl.psnc.vtl.portal.dto.layers.LayerCore;
import pl.psnc.vtl.portal.dto.layers.LayerCreationDto;
import pl.psnc.vtl.portal.dto.layers.LayerDto;
import pl.psnc.vtl.portal.dto.transcriptions.*;
import pl.psnc.vtl.portal.model.indexes.IndexType;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.verses.OcrMode;
import pl.psnc.vtl.portal.repositories.iiiftools.DeleteTranscriptionImagesEmailService;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsRepository;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.security.LoginCore;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Transactional
public class TranscriptionControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String TOKEN;
    private String TOKEN_FOR_USER_WITH_EXCEEDED_QUOTA;

    ObjectMapper objectMapper = new ObjectMapper();

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";

    @InjectMocks
    IIIFToolsRepository iiifToolsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeleteTranscriptionImagesEmailService deleteTranscriptionImagesEmailService;


    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
        TOKEN_FOR_USER_WITH_EXCEEDED_QUOTA = LogInService.getJWTToken(mvc, "userWithExceededQuota@wp.pl", "myPassword");
        ReflectionTestUtils.setField(iiifToolsRepository, "iiifToolsUrl", "http://localhost:6666/");
    }


    @Test
    public void shouldReturnNothingWhenNoAuthorized()
            throws Exception {
        LoginCore loginCore = new LoginCore();
        loginCore.setEmail(EMAIL);
        loginCore.setPassword("pass123W@");

        ResultActions result = mvc.perform(
                post("/api/auth/sign-in", loginCore).content(objectMapper.writeValueAsString(loginCore))
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }


    @Test
    public void shouldReturnTranscription()
            throws Exception {
        Integer transcriptionId = 1;

        mvc.perform(get("/api/transcriptions/{id}", transcriptionId).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN)).andExpect(status().isOk()).andExpect(
                jsonPath("id", is(transcriptionId)))
                .andExpect(jsonPath("titleLabel", is("Czerwony Kapturek")))
                .andExpect(jsonPath("authorsLabel", is("Bogusław Linda, Bruce Lee")))
                .andExpect(jsonPath("dateCreated", notNullValue()))
                .andExpect(jsonPath("creator.email", is("email2@wp.pl")))
                .andExpect(jsonPath("lastModificationDate", notNullValue()))
                .andExpect(jsonPath("creationMode", is("web-form")))

                .andExpect(jsonPath("analyticInfo.id", is(1)))
                .andExpect(jsonPath("analyticInfo.title", is("Czerwony Kapturek")))
                .andExpect(jsonPath("analyticInfo.actors", hasSize(2)))
                .andExpect(jsonPath("analyticInfo.language.code", is("pol")))

                .andExpect(jsonPath("monographicInfo.id", is(1)))
                .andExpect(jsonPath("monographicInfo.title", is("Some title")))
                .andExpect(jsonPath("monographicInfo.biblType.code", is("print")))
                .andExpect(jsonPath("monographicInfo.actors", hasSize(3)))
                .andExpect(jsonPath("monographicInfo.chapterNo", is(1)))
                .andExpect(jsonPath("monographicInfo.volumeNo", is(1)))
                .andExpect(jsonPath("monographicInfo.issueNo", is(1)))
                .andExpect(jsonPath("monographicInfo.pageRange", is("1")))
                .andExpect(jsonPath("monographicInfo.pages", is(1)))
                .andExpect(jsonPath("monographicInfo.publisher", is("publisher")))
                .andExpect(jsonPath("monographicInfo.publicationPlace", is("publicationPlace")))
                .andExpect(jsonPath("monographicInfo.publicationDate", is("publicationDate")))
                .andExpect(jsonPath("monographicInfo.isbn", is("isbn")))
                .andExpect(jsonPath("monographicInfo.eisbn", is("eisbn")))
                .andExpect(jsonPath("monographicInfo.issn", is("issn")))
                .andExpect(jsonPath("monographicInfo.eissn", is("eissn")))

                .andExpect(jsonPath("profileDesc.id", is(1)))
                .andExpect(jsonPath("profileDesc.summary", is("some summary")))
                .andExpect(jsonPath("profileDesc.keywords", hasSize(2)))

                .andExpect(jsonPath("hidden", is(false)))
                .andExpect(jsonPath("percentOfTranscribed", is(4)))
                .andExpect(jsonPath("percentOfVerified", is(4)))

                .andExpect(jsonPath("pages.list[0].pageNo", is(1)));
    }


    @Test
    public void shouldReturnTranscriptionProgress()
            throws Exception {
        Integer transcriptionId = 2;

        mvc.perform(get("/api/transcriptions/{id}", transcriptionId).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("percentOfTranscribed", is(25))).andExpect(jsonPath("percentOfVerified", is(12)));
    }


    @Test
    public void shouldReturnContinuationAfterPageRecognition()
            throws Exception {
        Integer transcriptionId = 2;
        Integer pageId = 5;

        mvc.perform(
                put("/api/transcriptions/{transcriptionId}/pages/{pageId}/recognize", transcriptionId, pageId).param(
                        "lang", "eng").param("ocrMode", OcrMode.OVERRIDE.name())
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        mvc.perform(get("/api/transcriptions/{id}", transcriptionId).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("continuation.lastPageId", is(5)))
                .andExpect(jsonPath("continuation.state", is("in-progress")));
    }

    @Test
    public void shouldReturnContinuationAsFirstPageWhenTranscribingNotStarted()
            throws Exception {
        Integer transcriptionId = 1;

        mvc.perform(get("/api/transcriptions/{id}", transcriptionId).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("continuation.lastPageId", is(1)))
                .andExpect(jsonPath("continuation.state", is("not-started")));
    }


    @Test
    public void shouldNotReturnContinuationForUnauthenticatedUser()
            throws Exception {
        Integer transcriptionId = 1;

        mvc.perform(get("/api/transcriptions/{id}", transcriptionId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("continuation", nullValue()));
    }


    @Test
    public void shouldNotReturnTranscriptionWhenNotExist()
            throws Exception {
        Integer transcriptionId = 10001;

        mvc.perform(get("/api/transcriptions/{id}", transcriptionId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }


    @Test
    public void shouldReturnAllPublicTranscriptionsOrderedByTitle()
            throws Exception {
        mvc.perform(get("/api/transcriptions").param("order", SearchOrder.TITLE.getValue())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(41)))
                .andExpect(jsonPath("page", is(1)))
                .andExpect(jsonPath("transcriptions", hasSize(4)))
                .andExpect(jsonPath("transcriptions[0].id", is(31)))
                .andExpect(jsonPath("transcriptions[0].titleLabel", is("Artykuł 1")))
                .andExpect(jsonPath("transcriptions[0].pages", notNullValue()))
                .andExpect(jsonPath("transcriptions[1].id", is(40)))
                .andExpect(jsonPath("transcriptions[1].titleLabel", is("Artykuł 10")))
                .andExpect(jsonPath("transcriptions[1].pages", notNullValue()))
                .andExpect(jsonPath("transcriptions[2].id", is(41)))
                .andExpect(jsonPath("transcriptions[2].titleLabel", is("Artykuł 11")))
                .andExpect(jsonPath("transcriptions[2].pages", notNullValue()))
                .andExpect(jsonPath("transcriptions[3].id", is(34)))
                .andExpect(jsonPath("transcriptions[3].titleLabel", is("Artykuł 4")))
                .andExpect(jsonPath("transcriptions[3].pages", notNullValue()));
    }


    @Test
    public void shouldReturnAllTranscriptionsForTheUserOrderedByTitle()
            throws Exception {
        mvc.perform(get("/api/transcriptions").param("order", SearchOrder.TITLE.getValue())
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(42)))
                .andExpect(jsonPath("page", is(1)))
                .andExpect(jsonPath("transcriptions", hasSize(4)))
                .andExpect(jsonPath("transcriptions[0].id", is(31)))
                .andExpect(jsonPath("transcriptions[0].titleLabel", is("Artykuł 1")))
                .andExpect(jsonPath("transcriptions[0].pages", notNullValue()))
                .andExpect(jsonPath("transcriptions[1].id", is(40)))
                .andExpect(jsonPath("transcriptions[1].titleLabel", is("Artykuł 10")))
                .andExpect(jsonPath("transcriptions[1].pages", notNullValue()))
                .andExpect(jsonPath("transcriptions[2].id", is(41)))
                .andExpect(jsonPath("transcriptions[2].titleLabel", is("Artykuł 11")))
                .andExpect(jsonPath("transcriptions[2].pages", notNullValue()))
                .andExpect(jsonPath("transcriptions[3].id", is(34)))
                .andExpect(jsonPath("transcriptions[3].titleLabel", is("Artykuł 4")))
                .andExpect(jsonPath("transcriptions[3].pages", notNullValue()));
    }


    @Test
    public void shouldReturnAllPublicTranscriptionsFilteredByQ()
            throws Exception {
        mvc.perform(get("/api/transcriptions/search").param("q", "lalka")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(4)))
                .andExpect(jsonPath("page", is(1)))
                .andExpect(jsonPath("searchResults", hasSize(4)))
                .andExpect(jsonPath("searchResults[0].transcription.id", is(6)))
                .andExpect(jsonPath("searchResults[0].transcription.titleLabel", is("Miś i Lalka")))
                .andExpect(jsonPath("searchResults[0].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[1].transcription.id", is(5)))
                .andExpect(jsonPath("searchResults[1].transcription.titleLabel", is("Lalka i Miś")))
                .andExpect(jsonPath("searchResults[1].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[2].transcription.id", is(4)))
                .andExpect(jsonPath("searchResults[2].transcription.titleLabel", is("Lalka")))
                .andExpect(jsonPath("searchResults[2].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[3].transcription.id", is(2)))
                .andExpect(jsonPath("searchResults[3].transcription.titleLabel", is("Lalka")))
                .andExpect(jsonPath("searchResults[3].transcription.pages", notNullValue()));

        mvc.perform(get("/api/transcriptions/search").param("q", "contactEmail@example.org")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(2)))
                .andExpect(jsonPath("page", is(1)))
                .andExpect(jsonPath("searchResults", hasSize(2)))
                .andExpect(jsonPath("searchResults[0].transcription.id", is(2)))
                .andExpect(jsonPath("searchResults[0].transcription.titleLabel", is("Lalka")))
                .andExpect(jsonPath("searchResults[0].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[1].transcription.id", is(1)))
                .andExpect(jsonPath("searchResults[1].transcription.titleLabel", is("Czerwony Kapturek")))
                .andExpect(jsonPath("searchResults[1].transcription.pages", notNullValue()));

        mvc.perform(get("/api/transcriptions/search").param("q", "Ala")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(3)))
                .andExpect(jsonPath("page", is(1)))
                .andExpect(jsonPath("searchResults", hasSize(3)))
                .andExpect(jsonPath("searchResults[0].transcription.id", is(3)))
                .andExpect(jsonPath("searchResults[0].transcription.titleLabel", is("Ogniem i mieczem")))
                .andExpect(jsonPath("searchResults[0].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[1].transcription.id", is(2)))
                .andExpect(jsonPath("searchResults[1].transcription.titleLabel", is("Lalka")))
                .andExpect(jsonPath("searchResults[1].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[2].transcription.id", is(1)))
                .andExpect(jsonPath("searchResults[2].transcription.titleLabel", is("Czerwony Kapturek")))
                .andExpect(jsonPath("searchResults[2].transcription.pages", notNullValue()));

    }


    @Test
    public void shouldReturnAllTranscriptionsForTheUserFilteredByQ()
            throws Exception {
        mvc.perform(get("/api/transcriptions/search").param("q", "lalka")
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(5)))
                .andExpect(jsonPath("page", is(1)))
                .andExpect(jsonPath("searchResults", hasSize(4)))
                .andExpect(jsonPath("searchResults[0].transcription.id", is(7)))
                .andExpect(jsonPath("searchResults[0].transcription.titleLabel", is("Lalka")))
                .andExpect(jsonPath("searchResults[0].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[1].transcription.id", is(6)))
                .andExpect(jsonPath("searchResults[1].transcription.titleLabel", is("Miś i Lalka")))
                .andExpect(jsonPath("searchResults[1].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[2].transcription.id", is(5)))
                .andExpect(jsonPath("searchResults[2].transcription.titleLabel", is("Lalka i Miś")))
                .andExpect(jsonPath("searchResults[2].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[3].transcription.id", is(4)))
                .andExpect(jsonPath("searchResults[3].transcription.titleLabel", is("Lalka")))
                .andExpect(jsonPath("searchResults[3].transcription.pages", notNullValue()));

        mvc.perform(get("/api/transcriptions/search").param("q", "Polska")
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(1)))
                .andExpect(jsonPath("page", is(1)))
                .andExpect(jsonPath("searchResults", hasSize(1)))
                .andExpect(jsonPath("searchResults[0].transcription.id", is(43)))
                .andExpect(jsonPath("searchResults[0].transcription.titleLabel", is("Test")))
                .andExpect(jsonPath("searchResults[0].transcription.pages", notNullValue()));
    }

    @Test
    public void shouldReturnAllTranscriptionsWithMatchesCountForTheUserFilteredByQByVerseContent() throws Exception {

        mvc.perform(get("/api/transcriptions/search").param("q", "Ala").param("type", "CONTENT")
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(3)))
                .andExpect(jsonPath("page", is(1)))
                .andExpect(jsonPath("searchResults", hasSize(3)))
                .andExpect(jsonPath("searchResults[0].transcription.id", is(3)))
                .andExpect(jsonPath("searchResults[0].transcription.titleLabel", is("Ogniem i mieczem")))
                .andExpect(jsonPath("searchResults[0].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[0].hitCount", is(3)))
                .andExpect(jsonPath("searchResults[1].transcription.id", is(2)))
                .andExpect(jsonPath("searchResults[1].transcription.titleLabel", is("Lalka")))
                .andExpect(jsonPath("searchResults[1].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[1].hitCount", is(7)))
                .andExpect(jsonPath("searchResults[2].transcription.id", is(1)))
                .andExpect(jsonPath("searchResults[2].transcription.titleLabel", is("Czerwony Kapturek")))
                .andExpect(jsonPath("searchResults[2].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[2].hitCount", is(10)));

    }


    @Test
    public void shouldReturnAllPublicTranscriptionsWithMatchesCountFilteredByQByVerseContent() throws Exception {

        mvc.perform(get("/api/transcriptions/search").param("q", "Ala").param("type", "CONTENT")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(3)))
                .andExpect(jsonPath("page", is(1)))
                .andExpect(jsonPath("searchResults", hasSize(3)))
                .andExpect(jsonPath("searchResults[0].transcription.id", is(3)))
                .andExpect(jsonPath("searchResults[0].transcription.titleLabel", is("Ogniem i mieczem")))
                .andExpect(jsonPath("searchResults[0].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[0].hitCount", is(3)))
                .andExpect(jsonPath("searchResults[1].transcription.id", is(2)))
                .andExpect(jsonPath("searchResults[1].transcription.titleLabel", is("Lalka")))
                .andExpect(jsonPath("searchResults[1].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[1].hitCount", is(7)))
                .andExpect(jsonPath("searchResults[2].transcription.id", is(1)))
                .andExpect(jsonPath("searchResults[2].transcription.titleLabel", is("Czerwony Kapturek")))
                .andExpect(jsonPath("searchResults[2].transcription.pages", notNullValue()))
                .andExpect(jsonPath("searchResults[2].hitCount", is(10)));

    }


    @Test
    public void shouldReturnMyTranscriptionsOrderedByCreationDate()
            throws Exception {
        mvc.perform(get("/api/my-transcriptions").param("order", SearchOrder.DATE_CREATED.getValue()).param("page", "2")
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(9)))
                .andExpect(jsonPath("page", is(2)))
                .andExpect(jsonPath("transcriptions", hasSize(4)))
                .andExpect(jsonPath("transcriptions[0].id", is(7)))
                .andExpect(jsonPath("transcriptions[0].titleLabel", is("Lalka")))
                .andExpect(jsonPath("transcriptions[0].pages", notNullValue()))
                .andExpect(jsonPath("transcriptions[1].id", is(6)))
                .andExpect(jsonPath("transcriptions[1].titleLabel", is("Miś i Lalka")))
                .andExpect(jsonPath("transcriptions[1].pages", notNullValue()))
                .andExpect(jsonPath("transcriptions[2].id", is(4)))
                .andExpect(jsonPath("transcriptions[2].titleLabel", is("Lalka")))
                .andExpect(jsonPath("transcriptions[2].pages", notNullValue()))
                .andExpect(jsonPath("transcriptions[3].id", is(2)))
                .andExpect(jsonPath("transcriptions[3].titleLabel", is("Lalka")))
                .andExpect(jsonPath("transcriptions[3].pages", notNullValue()));
    }


    @Test
    public void shouldReturnMyTranscriptionsOrderedByCreationDateAndFilteredByQ()
            throws Exception {
        mvc.perform(get("/api/my-transcriptions").param("order", SearchOrder.DATE_CREATED.getValue()).param("q", "koń")
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(2)))
                .andExpect(jsonPath("page", is(1)))
                .andExpect(jsonPath("transcriptions", hasSize(2)))
                .andExpect(jsonPath("transcriptions[0].id", is(4)))
                .andExpect(jsonPath("transcriptions[0].titleLabel", is("Lalka")))
                .andExpect(jsonPath("transcriptions[0].pages", notNullValue()))
                .andExpect(jsonPath("transcriptions[1].id", is(2)))
                .andExpect(jsonPath("transcriptions[1].titleLabel", is("Lalka")))
                .andExpect(jsonPath("transcriptions[1].pages", notNullValue()));

        mvc.perform(get("/api/my-transcriptions").param("order", SearchOrder.DATE_CREATED.getValue()).param("q", "description")
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(9)))
                .andExpect(jsonPath("page", is(1)))
                .andExpect(jsonPath("transcriptions", hasSize(4)))
                .andExpect(jsonPath("transcriptions[0].id", is(43)))
                .andExpect(jsonPath("transcriptions[0].titleLabel", is("Test")))
                .andExpect(jsonPath("transcriptions[0].pages", notNullValue()))
                .andExpect(jsonPath("transcriptions[1].id", is(11)))
                .andExpect(jsonPath("transcriptions[1].titleLabel", is("Rozdział 11")))
                .andExpect(jsonPath("transcriptions[1].pages", notNullValue()))
                .andExpect(jsonPath("transcriptions[2].id", is(10)))
                .andExpect(jsonPath("transcriptions[2].titleLabel", is("Rozdział 10")))
                .andExpect(jsonPath("transcriptions[2].pages", notNullValue()))
                .andExpect(jsonPath("transcriptions[3].id", is(9)))
                .andExpect(jsonPath("transcriptions[3].titleLabel", is("Rozdział 9")))
                .andExpect(jsonPath("transcriptions[3].pages", notNullValue()));
    }


    @Test
    public void shouldReturnUnauthorizedForMyTranscription() throws Exception {
        mvc.perform(get("/api/my-transcriptions").param("order", SearchOrder.DATE_CREATED.getValue())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }


    @Test
    public void shouldReturnUserTranscriptions()
            throws Exception {
        String superadminToken = LogInService.getJWTToken(mvc, "superadmin@wp.pl", "superadmin");

        mvc.perform(get("/api/user-transcriptions").param("ownerId", "3")
                .header("Authorization", superadminToken).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(4)))
                .andExpect(jsonPath("ownerEmail", is("email@wp.pl")))
                .andExpect(jsonPath("page", is(1)))
                .andExpect(jsonPath("transcriptions", hasSize(4)))
                .andExpect(jsonPath("transcriptions[0].id", is(43)))
                .andExpect(jsonPath("transcriptions[0].titleLabel", is("Test")))
                .andExpect(jsonPath("transcriptions[0].pages", is(nullValue())))
                .andExpect(jsonPath("transcriptions[1].id", is(6)))
                .andExpect(jsonPath("transcriptions[1].titleLabel", is("Miś i Lalka")))
                .andExpect(jsonPath("transcriptions[1].pages", is(nullValue())))
                .andExpect(jsonPath("transcriptions[2].id", is(4)))
                .andExpect(jsonPath("transcriptions[2].titleLabel", is("Lalka")))
                .andExpect(jsonPath("transcriptions[2].pages", is(nullValue())))
                .andExpect(jsonPath("transcriptions[3].id", is(2)))
                .andExpect(jsonPath("transcriptions[3].titleLabel", is("Lalka")))
                .andExpect(jsonPath("transcriptions[3].pages", is(nullValue())));
    }


    @Test
    public void shouldReturnUnauthorizedForUserTranscription() throws Exception {
        mvc.perform(get("/api/user-transcriptions").param("ownerId", "3").param("order",
                SearchOrder.DATE_CREATED.getValue())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }


    @Test
    public void shouldReturnForbiddenForUserTranscription() throws Exception {
        mvc.perform(get("/api/user-transcriptions").param("ownerId", "3").param("order",
                SearchOrder.DATE_CREATED.getValue())
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }


    @Test
    public void shouldDeleteTranscriptionWhenExists()
            throws Exception {
        Long id = 6L;

        mvc.perform(get("/api/transcriptions/{id}", id).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        mvc.perform(delete("/api/transcriptions/{id}", id).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        mvc.perform(get("/api/transcriptions/{id}", id).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }


    @Test
    public void shouldNotDeleteTranscriptionWhenHasNoPermission()
            throws Exception {
        Long id = 3L;

        String token = LogInService.getJWTToken(mvc, "email3@wp.pl", "myPassword");

        mvc.perform(delete("/api/transcriptions/{id}", id).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }


    @Test
    public void shouldNotDeleteTranscriptionWhenDoNotExist()
            throws Exception {
        Long id = 999L;

        mvc.perform(delete("/api/transcriptions/{id}", id).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }


    @Test
    public void shouldReturn400WhenBadDeleteRequest()
            throws Exception {

        String stringRequest = "stringId";

        mvc.perform(delete("/api/transcriptions/{id}", stringRequest).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }


    @Test
    public void shouldAddTranscription()
            throws Exception {
        TranscriptionCore newTranscription = new TranscriptionCore();

        //analyticInfo
        AnalyticInfoCore analyticInfo = new AnalyticInfoCore();
        String analyticInfoTitle = "analyticInfoTitle";
        String analyticInfoLanguageCode = "eng";
        String analyticInfoActor1Name = "analyticInfoActor1Name";
        String analyticInfoActor1RoleCode = "author";
        analyticInfo.setTitle(analyticInfoTitle);

        LanguageId analyticInfoLanguage = new LanguageId();
        analyticInfoLanguage.setCode(analyticInfoLanguageCode);
        analyticInfo.setLanguage(analyticInfoLanguage);

        ActorCore analyticInfoActor = new ActorCore();
        analyticInfoActor.setName(analyticInfoActor1Name);
        ActorRoleId analyticInfoActor1RoleId = new ActorRoleId();
        analyticInfoActor1RoleId.setCode(analyticInfoActor1RoleCode);
        analyticInfoActor.setRole(analyticInfoActor1RoleId);

        analyticInfo.setActors(Arrays.asList(analyticInfoActor));

        //monographicInfo
        MonographicInfoCore monographicInfo = new MonographicInfoCore();

        String monographicInfoTitle = "monographicInfoTitle";
        String monographicInfoBiblTypeCode = "print";
        String monographicInfoActor1Name = "monographicInfoActor1Name";
        String monographicInfoActor1Role = "author";
        String monographicInfoLanguageCode = "eng";
        Integer monographicInfoChapterNo = 1;
        Integer monographicInfoVolumeNo = 2;
        Integer monographicInfoIssueNo = 3;
        String monographicInfoPageRange = "4-5";
        Integer monographicInfoPages = 5;
        String monographicInfoPublisher = "monographicInfoPublisher";
        String monographicInfoPublicationPlace = "monographicInfoPublicationPlace";
        String monographicInfoPublicationDate = "monographicInfoPublicationDate";
        String monographicInfoIsbn = "monographicInfoIsbn";
        String monographicInfoEisbn = "monographicInfoEisbn";
        String monographicInfoIssn = "monographicInfoIssn";
        String monographicInfoEissn = "monographicInfoEissn";

        monographicInfo.setTitle(monographicInfoTitle);

        BiblTypeId monographicInfoBiblTypeId = new BiblTypeId();
        monographicInfoBiblTypeId.setCode(monographicInfoBiblTypeCode);
        monographicInfo.setBiblType(monographicInfoBiblTypeId);

        LanguageId monographicInfoLanguage = new LanguageId();
        monographicInfoLanguage.setCode(monographicInfoLanguageCode);
        monographicInfo.setLanguage(monographicInfoLanguage);

        ActorCore monographicInfoActor = new ActorCore();
        monographicInfoActor.setName(monographicInfoActor1Name);
        ActorRoleId monographicInfoActorId = new ActorRoleId();
        monographicInfoActorId.setCode(monographicInfoActor1Role);
        monographicInfoActor.setRole(monographicInfoActorId);
        monographicInfo.setActors(Arrays.asList(monographicInfoActor));

        monographicInfo.setChapterNo(monographicInfoChapterNo);
        monographicInfo.setVolumeNo(monographicInfoVolumeNo);
        monographicInfo.setIssueNo(monographicInfoIssueNo);
        monographicInfo.setPageRange(monographicInfoPageRange);
        monographicInfo.setPages(monographicInfoPages);
        monographicInfo.setPublisher(monographicInfoPublisher);
        monographicInfo.setPublicationPlace(monographicInfoPublicationPlace);
        monographicInfo.setPublicationDate(monographicInfoPublicationDate);
        monographicInfo.setIsbn(monographicInfoIsbn);
        monographicInfo.setEisbn(monographicInfoEisbn);
        monographicInfo.setIssn(monographicInfoIssn);
        monographicInfo.setEissn(monographicInfoEissn);

        //profileDesc
        ProfileDescCore profileDesc = new ProfileDescCore();
        String summary = "summary";
        String link1 = "http://example.com";
        String keyWord1 = "keyWord1";

        profileDesc.setKeywords(Arrays.asList(keyWord1));
        profileDesc.setLinks(Arrays.asList(link1));
        profileDesc.setSummary(summary);

        //Transcription info:
        TranscriptionInfoCore transcriptionInfoCore = new TranscriptionInfoCore();
        String releaseDate = "2021-11-11:11:11:11 - 2022-02-2:2:2:2";
        String collectionName = "collection1";
        String name = "John";
        String surname = "Smith";
        String email = "john_is_a@example.org";
        String description = "Lorem ipsum";
        String documentSrc = "documentSrc";
        String docTypes = "docTypes";
        String guideLines = "guideLines";
        String imgSrc = "imgSrc";
        transcriptionInfoCore.setReleaseDate(releaseDate);
        transcriptionInfoCore.setCollectionName(collectionName);
        transcriptionInfoCore.setContactName(name);
        transcriptionInfoCore.setContactSurname(surname);
        transcriptionInfoCore.setContactEmail(email);
        transcriptionInfoCore.setDescription(description);
        transcriptionInfoCore.setDocumentSrc(documentSrc);
        transcriptionInfoCore.setDocumentTypes(docTypes);
        transcriptionInfoCore.setGuidelines(guideLines);
        transcriptionInfoCore.setImgSrc(imgSrc);
        newTranscription.setTranscriptionInfo(transcriptionInfoCore);

        newTranscription.setAnalyticInfo(analyticInfo);
        newTranscription.setMonographicInfo(monographicInfo);
        newTranscription.setProfileDesc(profileDesc);

        newTranscription.setAcceptedRegulations(true);

        String jsonResponse = mvc.perform(post("/api/transcriptions", newTranscription).header("Authorization", TOKEN)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(newTranscription))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("dateCreated", notNullValue())).andExpect(jsonPath("creator.email", is(EMAIL)))
                .andExpect(jsonPath("lastModificationDate", notNullValue()))
                .andExpect(jsonPath("creationMode", is("web-form")))
                .andExpect(jsonPath("externalUrl", isEmptyOrNullString()))
                .andExpect(jsonPath("externalId", isEmptyOrNullString()))

                .andExpect(jsonPath("titleLabel", is(analyticInfoTitle)))
                .andExpect(jsonPath("authorsLabel", is(analyticInfoActor1Name)))

                .andExpect(jsonPath("analyticInfo.title", is(analyticInfoTitle)))
                .andExpect(jsonPath("analyticInfo.actors[0].name", is(analyticInfoActor1Name)))
                .andExpect(jsonPath("analyticInfo.actors[0].role.code", is(analyticInfoActor1RoleCode)))
                .andExpect(jsonPath("analyticInfo.language.code", is(analyticInfoLanguageCode)))

                .andExpect(jsonPath("monographicInfo.title", is(monographicInfoTitle)))
                .andExpect(jsonPath("monographicInfo.biblType.code", is(monographicInfoBiblTypeCode)))
                .andExpect(jsonPath("monographicInfo.actors[0].role.code", is(monographicInfoActor1Role)))
                .andExpect(jsonPath("monographicInfo.actors[0].name", is(monographicInfoActor1Name)))
                .andExpect(jsonPath("monographicInfo.language.code", is(monographicInfoLanguageCode)))
                .andExpect(jsonPath("monographicInfo.chapterNo", is(monographicInfoChapterNo)))
                .andExpect(jsonPath("monographicInfo.volumeNo", is(monographicInfoVolumeNo)))
                .andExpect(jsonPath("monographicInfo.issueNo", is(monographicInfoIssueNo)))
                .andExpect(jsonPath("monographicInfo.pageRange", is(monographicInfoPageRange)))
                .andExpect(jsonPath("monographicInfo.pages", is(monographicInfoPages)))
                .andExpect(jsonPath("monographicInfo.publisher", is(monographicInfoPublisher)))
                .andExpect(jsonPath("monographicInfo.publicationPlace", is(monographicInfoPublicationPlace)))
                .andExpect(jsonPath("monographicInfo.publicationDate", is(monographicInfoPublicationDate)))
                .andExpect(jsonPath("monographicInfo.isbn", is(monographicInfoIsbn)))
                .andExpect(jsonPath("monographicInfo.eisbn", is(monographicInfoEisbn)))
                .andExpect(jsonPath("monographicInfo.issn", is(monographicInfoIssn)))
                .andExpect(jsonPath("monographicInfo.eissn", is(monographicInfoEissn)))

                .andExpect(jsonPath("profileDesc.links[0]", is(link1)))
                .andExpect(jsonPath("profileDesc.summary", is(summary)))
                .andExpect(jsonPath("profileDesc.keywords[0]", is(keyWord1)))
                .andExpect(jsonPath("transcriptionInfo.releaseDate", is(releaseDate)))
                .andExpect(jsonPath("transcriptionInfo.collectionName", is(collectionName)))
                .andExpect(jsonPath("transcriptionInfo.contactName", is(name)))
                .andExpect(jsonPath("transcriptionInfo.contactSurname", is(surname)))
                .andExpect(jsonPath("transcriptionInfo.contactEmail", is(email)))
                .andExpect(jsonPath("transcriptionInfo.description", is(description)))
                .andExpect(jsonPath("transcriptionInfo.documentSrc", is(documentSrc)))
                .andExpect(jsonPath("transcriptionInfo.documentTypes", is(docTypes)))
                .andExpect(jsonPath("transcriptionInfo.guideLines", is(guideLines)))
                .andExpect(jsonPath("transcriptionInfo.imgSrc", is(imgSrc)))

                .andExpect(jsonPath("hidden", is(true)))
                .andReturn().getResponse().getContentAsString();
        Transcription transcription =
                TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, Transcription.class);

        mvc.perform(get("/api/transcriptions/{id}", transcription.getId()).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("dateCreated", notNullValue())).andExpect(jsonPath("creator.email", is(EMAIL)))
                .andExpect(jsonPath("lastModificationDate", notNullValue()))
                .andExpect(jsonPath("creationMode", is("web-form")))
                .andExpect(jsonPath("externalUrl", isEmptyOrNullString()))
                .andExpect(jsonPath("externalId", isEmptyOrNullString()))

                .andExpect(jsonPath("titleLabel", is(analyticInfoTitle)))
                .andExpect(jsonPath("authorsLabel", is(analyticInfoActor1Name)))

                .andExpect(jsonPath("analyticInfo.title", is(analyticInfoTitle)))
                .andExpect(jsonPath("analyticInfo.actors[0].name", is(analyticInfoActor1Name)))
                .andExpect(jsonPath("analyticInfo.actors[0].role.code", is(analyticInfoActor1RoleCode)))
                .andExpect(jsonPath("analyticInfo.language.code", is(analyticInfoLanguageCode)))

                .andExpect(jsonPath("monographicInfo.title", is(monographicInfoTitle)))
                .andExpect(jsonPath("monographicInfo.biblType.code", is(monographicInfoBiblTypeCode)))
                .andExpect(jsonPath("monographicInfo.actors[0].role.code", is(monographicInfoActor1Role)))
                .andExpect(jsonPath("monographicInfo.actors[0].name", is(monographicInfoActor1Name)))
                .andExpect(jsonPath("monographicInfo.language.code", is(monographicInfoLanguageCode)))
                .andExpect(jsonPath("monographicInfo.chapterNo", is(monographicInfoChapterNo)))
                .andExpect(jsonPath("monographicInfo.volumeNo", is(monographicInfoVolumeNo)))
                .andExpect(jsonPath("monographicInfo.issueNo", is(monographicInfoIssueNo)))
                .andExpect(jsonPath("monographicInfo.pageRange", is(monographicInfoPageRange)))
                .andExpect(jsonPath("monographicInfo.pages", is(monographicInfoPages)))
                .andExpect(jsonPath("monographicInfo.publisher", is(monographicInfoPublisher)))
                .andExpect(jsonPath("monographicInfo.publicationPlace", is(monographicInfoPublicationPlace)))
                .andExpect(jsonPath("monographicInfo.publicationDate", is(monographicInfoPublicationDate)))
                .andExpect(jsonPath("monographicInfo.isbn", is(monographicInfoIsbn)))
                .andExpect(jsonPath("monographicInfo.eisbn", is(monographicInfoEisbn)))
                .andExpect(jsonPath("monographicInfo.issn", is(monographicInfoIssn)))
                .andExpect(jsonPath("monographicInfo.eissn", is(monographicInfoEissn)))

                .andExpect(jsonPath("transcriptionInfo.releaseDate", is(releaseDate)))
                .andExpect(jsonPath("transcriptionInfo.collectionName", is(collectionName)))
                .andExpect(jsonPath("transcriptionInfo.contactName", is(name)))
                .andExpect(jsonPath("transcriptionInfo.contactSurname", is(surname)))
                .andExpect(jsonPath("transcriptionInfo.contactEmail", is(email)))
                .andExpect(jsonPath("transcriptionInfo.description", is(description)))
                .andExpect(jsonPath("transcriptionInfo.documentSrc", is(documentSrc)))
                .andExpect(jsonPath("transcriptionInfo.documentTypes", is(docTypes)))
                .andExpect(jsonPath("transcriptionInfo.guideLines", is(guideLines)))
                .andExpect(jsonPath("transcriptionInfo.imgSrc", is(imgSrc)))

                .andExpect(jsonPath("profileDesc.links[0]", is(link1)))
                .andExpect(jsonPath("profileDesc.summary", is(summary)))
                .andExpect(jsonPath("profileDesc.keywords[0]", is(keyWord1)))

                .andExpect(jsonPath("hidden", is(true)));
    }


    @Test
    public void shouldNotAddTranscriptionWithoutAnyTitle()
            throws Exception {
        TranscriptionCore newTranscription = new TranscriptionCore();

        //analyticInfo
        AnalyticInfoCore analyticInfo = new AnalyticInfoCore();
        String analyticInfoLanguageCode = "eng";
        String analyticInfoActor1Name = "analyticInfoActor1Name";
        String analyticInfoActor1RoleCode = "author";

        LanguageId analyticInfoLanguage = new LanguageId();
        analyticInfoLanguage.setCode(analyticInfoLanguageCode);
        analyticInfo.setLanguage(analyticInfoLanguage);

        ActorCore analyticInfoActor = new ActorCore();
        analyticInfoActor.setName(analyticInfoActor1Name);
        ActorRoleId analyticInfoActor1RoleId = new ActorRoleId();
        analyticInfoActor1RoleId.setCode(analyticInfoActor1RoleCode);
        analyticInfoActor.setRole(analyticInfoActor1RoleId);

        analyticInfo.setActors(Arrays.asList(analyticInfoActor));

        //monographicInfo
        MonographicInfoCore monographicInfo = new MonographicInfoCore();

        String monographicInfoBiblTypeCode = "print";
        String monographicInfoActor1Name = "monographicInfoActor1Name";
        String monographicInfoActor1Role = "author";
        String monographicInfoLanguageCode = "eng";
        Integer monographicInfoChapterNo = 1;
        Integer monographicInfoVolumeNo = 2;
        Integer monographicInfoIssueNo = 3;
        String monographicInfoPageRange = "4-5";
        Integer monographicInfoPages = 5;
        String monographicInfoPublisher = "monographicInfoPublisher";
        String monographicInfoPublicationPlace = "monographicInfoPublicationPlace";
        String monographicInfoPublicationDate = "monographicInfoPublicationDate";
        String monographicInfoIsbn = "monographicInfoIsbn";
        String monographicInfoEisbn = "monographicInfoEisbn";
        String monographicInfoIssn = "monographicInfoIssn";
        String monographicInfoEissn = "monographicInfoEissn";

        BiblTypeId monographicInfoBiblTypeId = new BiblTypeId();
        monographicInfoBiblTypeId.setCode(monographicInfoBiblTypeCode);
        monographicInfo.setBiblType(monographicInfoBiblTypeId);

        LanguageId monographicInfoLanguage = new LanguageId();
        monographicInfoLanguage.setCode(monographicInfoLanguageCode);
        monographicInfo.setLanguage(monographicInfoLanguage);

        ActorCore monographicInfoActor = new ActorCore();
        monographicInfoActor.setName(monographicInfoActor1Name);
        ActorRoleId monographicInfoActorId = new ActorRoleId();
        monographicInfoActorId.setCode(monographicInfoActor1Role);
        monographicInfoActor.setRole(monographicInfoActorId);
        monographicInfo.setActors(Arrays.asList(monographicInfoActor));

        monographicInfo.setChapterNo(monographicInfoChapterNo);
        monographicInfo.setVolumeNo(monographicInfoVolumeNo);
        monographicInfo.setIssueNo(monographicInfoIssueNo);
        monographicInfo.setPageRange(monographicInfoPageRange);
        monographicInfo.setPages(monographicInfoPages);
        monographicInfo.setPublisher(monographicInfoPublisher);
        monographicInfo.setPublicationPlace(monographicInfoPublicationPlace);
        monographicInfo.setPublicationDate(monographicInfoPublicationDate);
        monographicInfo.setIsbn(monographicInfoIsbn);
        monographicInfo.setEisbn(monographicInfoEisbn);
        monographicInfo.setIssn(monographicInfoIssn);
        monographicInfo.setEissn(monographicInfoEissn);

        //profileDesc
        ProfileDescCore profileDesc = new ProfileDescCore();
        String summary = "summary";
        String link1 = "link1";
        String keyWord1 = "keyWord1";

        profileDesc.setKeywords(Arrays.asList(keyWord1));
        profileDesc.setLinks(Arrays.asList(link1));
        profileDesc.setSummary(summary);

        newTranscription.setAnalyticInfo(analyticInfo);
        newTranscription.setMonographicInfo(monographicInfo);
        newTranscription.setProfileDesc(profileDesc);

        newTranscription.setAcceptedRegulations(true);

        mvc.perform(post("/api/transcriptions", newTranscription).header("Authorization", TOKEN)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(newTranscription))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['analyticInfo.title']", is("transcriptions.error.atLeastOneTitleIsRequired")))
                .andExpect(
                        jsonPath("$['monographicInfo.title']", is("transcriptions.error.atLeastOneTitleIsRequired")));

    }


    @Test
    public void shouldNotAddTranscriptionWithoutAcceptedRegulations()
            throws Exception {
        TranscriptionCore newTranscription = new TranscriptionCore();

        //analyticInfo
        AnalyticInfoCore analyticInfo = new AnalyticInfoCore();
        String analyticInfoLanguageCode = "eng";
        String analyticInfoActor1Name = "analyticInfoActor1Name";
        String analyticInfoActor1RoleCode = "author";

        LanguageId analyticInfoLanguage = new LanguageId();
        analyticInfoLanguage.setCode(analyticInfoLanguageCode);
        analyticInfo.setLanguage(analyticInfoLanguage);

        ActorCore analyticInfoActor = new ActorCore();
        analyticInfoActor.setName(analyticInfoActor1Name);
        ActorRoleId analyticInfoActor1RoleId = new ActorRoleId();
        analyticInfoActor1RoleId.setCode(analyticInfoActor1RoleCode);
        analyticInfoActor.setRole(analyticInfoActor1RoleId);

        analyticInfo.setActors(Arrays.asList(analyticInfoActor));

        //monographicInfo
        MonographicInfoCore monographicInfo = new MonographicInfoCore();

        String monographicInfoBiblTypeCode = "print";
        String monographicInfoActor1Name = "monographicInfoActor1Name";
        String monographicInfoActor1Role = "author";
        String monographicInfoLanguageCode = "eng";
        Integer monographicInfoChapterNo = 1;
        Integer monographicInfoVolumeNo = 2;
        Integer monographicInfoIssueNo = 3;
        String monographicInfoPageRange = "4-5";
        Integer monographicInfoPages = 5;
        String monographicInfoPublisher = "monographicInfoPublisher";
        String monographicInfoPublicationPlace = "monographicInfoPublicationPlace";
        String monographicInfoPublicationDate = "monographicInfoPublicationDate";
        String monographicInfoIsbn = "monographicInfoIsbn";
        String monographicInfoEisbn = "monographicInfoEisbn";
        String monographicInfoIssn = "monographicInfoIssn";
        String monographicInfoEissn = "monographicInfoEissn";

        BiblTypeId monographicInfoBiblTypeId = new BiblTypeId();
        monographicInfoBiblTypeId.setCode(monographicInfoBiblTypeCode);
        monographicInfo.setBiblType(monographicInfoBiblTypeId);

        LanguageId monographicInfoLanguage = new LanguageId();
        monographicInfoLanguage.setCode(monographicInfoLanguageCode);
        monographicInfo.setLanguage(monographicInfoLanguage);

        ActorCore monographicInfoActor = new ActorCore();
        monographicInfoActor.setName(monographicInfoActor1Name);
        ActorRoleId monographicInfoActorId = new ActorRoleId();
        monographicInfoActorId.setCode(monographicInfoActor1Role);
        monographicInfoActor.setRole(monographicInfoActorId);
        monographicInfo.setActors(Arrays.asList(monographicInfoActor));

        monographicInfo.setChapterNo(monographicInfoChapterNo);
        monographicInfo.setVolumeNo(monographicInfoVolumeNo);
        monographicInfo.setIssueNo(monographicInfoIssueNo);
        monographicInfo.setPageRange(monographicInfoPageRange);
        monographicInfo.setPages(monographicInfoPages);
        monographicInfo.setPublisher(monographicInfoPublisher);
        monographicInfo.setPublicationPlace(monographicInfoPublicationPlace);
        monographicInfo.setPublicationDate(monographicInfoPublicationDate);
        monographicInfo.setIsbn(monographicInfoIsbn);
        monographicInfo.setEisbn(monographicInfoEisbn);
        monographicInfo.setIssn(monographicInfoIssn);
        monographicInfo.setEissn(monographicInfoEissn);

        //profileDesc
        ProfileDescCore profileDesc = new ProfileDescCore();
        String summary = "summary";
        String link1 = "link1";
        String keyWord1 = "keyWord1";

        profileDesc.setKeywords(Arrays.asList(keyWord1));
        profileDesc.setLinks(Arrays.asList(link1));
        profileDesc.setSummary(summary);

        newTranscription.setAnalyticInfo(analyticInfo);
        newTranscription.setMonographicInfo(monographicInfo);
        newTranscription.setProfileDesc(profileDesc);

        mvc.perform(post("/api/transcriptions", newTranscription).header("Authorization", TOKEN)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(newTranscription))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("acceptedRegulations", is("transcriptions.error.fieldRequired")));
    }


    @Test
    public void shouldUpdateTranscription()
            throws Exception {
        int transcriptionId = 1;

        TranscriptionCore newTranscription = new TranscriptionCore();

        //analyticInfo
        AnalyticInfoCore analyticInfo = new AnalyticInfoCore();
        String analyticInfoTitle = "analyticInfoTitleUpdated";
        String analyticInfoLanguageCode = "pol";
        String analyticInfoActor1Name = "analyticInfoActor1Name";
        String analyticInfoActor1RoleCode = "editor";
        analyticInfo.setTitle(analyticInfoTitle);

        LanguageId analyticInfoLanguage = new LanguageId();
        analyticInfoLanguage.setCode(analyticInfoLanguageCode);
        analyticInfo.setLanguage(analyticInfoLanguage);

        ActorCore analyticInfoActor = new ActorCore();
        analyticInfoActor.setName(analyticInfoActor1Name);
        ActorRoleId analyticInfoActor1RoleId = new ActorRoleId();
        analyticInfoActor1RoleId.setCode(analyticInfoActor1RoleCode);
        analyticInfoActor.setRole(analyticInfoActor1RoleId);

        analyticInfo.setActors(Arrays.asList(analyticInfoActor));

        //monographicInfo
        MonographicInfoCore monographicInfo = new MonographicInfoCore();

        String monographicInfoTitle = "monographicInfoTitleUpdated";
        String monographicInfoBiblTypeCode = "print";
        String monographicInfoActor1Name = "monographicInfoActor1Name";
        String monographicInfoActor1Role = "author";
        String monographicInfoLanguageCode = "pol";
        Integer monographicInfoChapterNo = 1;
        Integer monographicInfoVolumeNo = 4;
        Integer monographicInfoIssueNo = 3;
        String monographicInfoPageRange = "4-5";
        Integer monographicInfoPages = 5;
        String monographicInfoPublisher = "monographicInfoPublisherUpdated";
        String monographicInfoPublicationPlace = "monographicInfoPublicationPlaceUpdated";
        String monographicInfoPublicationDate = "monographicInfoPublicationDateUpdated";
        String monographicInfoIsbn = "monographicInfoIsbn";
        String monographicInfoEisbn = "monographicInfoEisbn";
        String monographicInfoIssn = "monographicInfoIssn";
        String monographicInfoEissn = "monographicInfoEissn";

        monographicInfo.setTitle(monographicInfoTitle);

        BiblTypeId monographicInfoBiblTypeId = new BiblTypeId();
        monographicInfoBiblTypeId.setCode(monographicInfoBiblTypeCode);
        monographicInfo.setBiblType(monographicInfoBiblTypeId);

        LanguageId monographicInfoLanguage = new LanguageId();
        monographicInfoLanguage.setCode(monographicInfoLanguageCode);
        monographicInfo.setLanguage(monographicInfoLanguage);

        ActorCore monographicInfoActor = new ActorCore();
        monographicInfoActor.setName(monographicInfoActor1Name);
        ActorRoleId monographicInfoActorId = new ActorRoleId();
        monographicInfoActorId.setCode(monographicInfoActor1Role);
        monographicInfoActor.setRole(monographicInfoActorId);
        monographicInfo.setActors(Arrays.asList(monographicInfoActor));

        monographicInfo.setChapterNo(monographicInfoChapterNo);
        monographicInfo.setVolumeNo(monographicInfoVolumeNo);
        monographicInfo.setIssueNo(monographicInfoIssueNo);
        monographicInfo.setPageRange(monographicInfoPageRange);
        monographicInfo.setPages(monographicInfoPages);
        monographicInfo.setPublisher(monographicInfoPublisher);
        monographicInfo.setPublicationPlace(monographicInfoPublicationPlace);
        monographicInfo.setPublicationDate(monographicInfoPublicationDate);
        monographicInfo.setIsbn(monographicInfoIsbn);
        monographicInfo.setEisbn(monographicInfoEisbn);
        monographicInfo.setIssn(monographicInfoIssn);
        monographicInfo.setEissn(monographicInfoEissn);

        //profileDesc
        ProfileDescCore profileDesc = new ProfileDescCore();
        String summary = "summary";
        String link1 = "http://pcss.pl";
        String link2 = "http://example.com";
        String keyWord1 = "keyWordUpdated";
        String keyWord2 = "keyWord2";

        profileDesc.setKeywords(Arrays.asList(keyWord1, keyWord2));
        profileDesc.setLinks(Arrays.asList(link1, link2));
        profileDesc.setSummary(summary);

        newTranscription.setAnalyticInfo(analyticInfo);
        newTranscription.setMonographicInfo(monographicInfo);
        newTranscription.setProfileDesc(profileDesc);

        mvc.perform(put("/api/transcriptions/{transcriptionId}", transcriptionId).header("Authorization", TOKEN)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(newTranscription))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("dateCreated", notNullValue()))
                .andExpect(jsonPath("lastModificationDate", notNullValue()))
                .andExpect(jsonPath("creationMode", is("web-form")))
                .andExpect(jsonPath("externalUrl", isEmptyOrNullString()))
                .andExpect(jsonPath("externalId", isEmptyOrNullString()))

                .andExpect(jsonPath("titleLabel", is(analyticInfoTitle)))
                .andExpect(jsonPath("authorsLabel", is(analyticInfoActor1Name)))

                .andExpect(jsonPath("analyticInfo.title", is(analyticInfoTitle)))
                .andExpect(jsonPath("analyticInfo.actors[0].name", is(analyticInfoActor1Name)))
                .andExpect(jsonPath("analyticInfo.actors[0].role.code", is(analyticInfoActor1RoleCode)))
                .andExpect(jsonPath("analyticInfo.language.code", is(analyticInfoLanguageCode)))

                .andExpect(jsonPath("monographicInfo.title", is(monographicInfoTitle)))
                .andExpect(jsonPath("monographicInfo.biblType.code", is(monographicInfoBiblTypeCode)))
                .andExpect(jsonPath("monographicInfo.actors[0].role.code", is(monographicInfoActor1Role)))
                .andExpect(jsonPath("monographicInfo.actors[0].name", is(monographicInfoActor1Name)))
                .andExpect(jsonPath("monographicInfo.language.code", is(monographicInfoLanguageCode)))
                .andExpect(jsonPath("monographicInfo.chapterNo", is(monographicInfoChapterNo)))
                .andExpect(jsonPath("monographicInfo.volumeNo", is(monographicInfoVolumeNo)))
                .andExpect(jsonPath("monographicInfo.issueNo", is(monographicInfoIssueNo)))
                .andExpect(jsonPath("monographicInfo.pageRange", is(monographicInfoPageRange)))
                .andExpect(jsonPath("monographicInfo.pages", is(monographicInfoPages)))
                .andExpect(jsonPath("monographicInfo.publisher", is(monographicInfoPublisher)))
                .andExpect(jsonPath("monographicInfo.publicationPlace", is(monographicInfoPublicationPlace)))
                .andExpect(jsonPath("monographicInfo.publicationDate", is(monographicInfoPublicationDate)))
                .andExpect(jsonPath("monographicInfo.isbn", is(monographicInfoIsbn)))
                .andExpect(jsonPath("monographicInfo.eisbn", is(monographicInfoEisbn)))
                .andExpect(jsonPath("monographicInfo.issn", is(monographicInfoIssn)))
                .andExpect(jsonPath("monographicInfo.eissn", is(monographicInfoEissn)))

                .andExpect(jsonPath("profileDesc.links[0]", is(link1)))
                .andExpect(jsonPath("profileDesc.links[1]", is(link2)))
                .andExpect(jsonPath("profileDesc.summary", is(summary)))
                .andExpect(jsonPath("profileDesc.keywords[0]", is(keyWord1)))
                .andExpect(jsonPath("profileDesc.keywords[1]", is(keyWord2)));
    }

    @Test
    public void shouldUpdateTranscriptionMetadata()
            throws Exception {
        int transcriptionId = 1;
        String releaseDate = "2021-11-11:11:11:11 - 2022-02-2:2:2:2";
        String collectionName = "collection1";
        String name = "Johnnnnnn";
        String surname = "Smithhhhhhh";
        String email = "john_is_a@example.org";
        String description = "Lorem ipsum";
        String documentSrc = "documentSrc";
        String docTypes = "docTypes";
        String guideLines = "guideLines";
        String imgSrc = "imgSrc";
        TranscriptionInfoCore transcriptionInfoCore = new TranscriptionInfoCore();
        transcriptionInfoCore.setReleaseDate(releaseDate);
        transcriptionInfoCore.setCollectionName(collectionName);
        transcriptionInfoCore.setContactName(name);
        transcriptionInfoCore.setContactSurname(surname);
        transcriptionInfoCore.setContactEmail(email);
        transcriptionInfoCore.setDescription(description);
        transcriptionInfoCore.setDocumentSrc(documentSrc);
        transcriptionInfoCore.setDocumentTypes(docTypes);
        transcriptionInfoCore.setGuidelines(guideLines);
        transcriptionInfoCore.setImgSrc(imgSrc);

        MonographicInfoCore monographicInfo = new MonographicInfoCore();

        String monographicInfoTitle = "monographicInfoTitleUpdated";
        String monographicInfoBiblTypeCode = "print";
        String monographicInfoActor1Name = "monographicInfoActor1Name";
        String monographicInfoActor1Role = "author";
        String monographicInfoLanguageCode = "pol";
        Integer monographicInfoChapterNo = 1;
        Integer monographicInfoVolumeNo = 4;
        Integer monographicInfoIssueNo = 3;
        String monographicInfoPageRange = "4-5";
        Integer monographicInfoPages = 5;
        String monographicInfoPublisher = "monographicInfoPublisherUpdated";
        String monographicInfoPublicationPlace = "monographicInfoPublicationPlaceUpdated";
        String monographicInfoPublicationDate = "monographicInfoPublicationDateUpdated";
        String monographicInfoIsbn = "monographicInfoIsbn";
        String monographicInfoEisbn = "monographicInfoEisbn";
        String monographicInfoIssn = "monographicInfoIssn";
        String monographicInfoEissn = "monographicInfoEissn";

        monographicInfo.setTitle(monographicInfoTitle);

        BiblTypeId monographicInfoBiblTypeId = new BiblTypeId();
        monographicInfoBiblTypeId.setCode(monographicInfoBiblTypeCode);
        monographicInfo.setBiblType(monographicInfoBiblTypeId);

        LanguageId monographicInfoLanguage = new LanguageId();
        monographicInfoLanguage.setCode(monographicInfoLanguageCode);
        monographicInfo.setLanguage(monographicInfoLanguage);

        ActorCore monographicInfoActor = new ActorCore();
        monographicInfoActor.setName(monographicInfoActor1Name);
        ActorRoleId monographicInfoActorId = new ActorRoleId();
        monographicInfoActorId.setCode(monographicInfoActor1Role);
        monographicInfoActor.setRole(monographicInfoActorId);
        monographicInfo.setActors(Arrays.asList(monographicInfoActor));

        monographicInfo.setChapterNo(monographicInfoChapterNo);
        monographicInfo.setVolumeNo(monographicInfoVolumeNo);
        monographicInfo.setIssueNo(monographicInfoIssueNo);
        monographicInfo.setPageRange(monographicInfoPageRange);
        monographicInfo.setPages(monographicInfoPages);
        monographicInfo.setPublisher(monographicInfoPublisher);
        monographicInfo.setPublicationPlace(monographicInfoPublicationPlace);
        monographicInfo.setPublicationDate(monographicInfoPublicationDate);
        monographicInfo.setIsbn(monographicInfoIsbn);
        monographicInfo.setEisbn(monographicInfoEisbn);
        monographicInfo.setIssn(monographicInfoIssn);
        monographicInfo.setEissn(monographicInfoEissn);

        //profileDesc
        ProfileDescCore profileDesc = new ProfileDescCore();
        String summary = "summary";
        String link1 = "http://pcss.pl";
        String link2 = "http://example.com";
        String keyWord1 = "keyWordUpdated";
        String keyWord2 = "keyWord2";

        profileDesc.setKeywords(Arrays.asList(keyWord1, keyWord2));
        profileDesc.setLinks(Arrays.asList(link1, link2));
        profileDesc.setSummary(summary);


        TranscriptionMetadataCore transcriptionMetadataCore = new TranscriptionMetadataCore();
        transcriptionMetadataCore.setAnalyticInfo(getAnalyticInfoCore());
        transcriptionMetadataCore.setMonographicInfo(monographicInfo);
        transcriptionMetadataCore.setTranscriptionInfo(transcriptionInfoCore);
        transcriptionMetadataCore.setProfileDesc(profileDesc);
        String jsonResponse = mvc.perform(
                put("/api/transcriptionss/{transcriptionId}/metadata", transcriptionId)
                        .header("Authorization", TOKEN)
                        .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(transcriptionMetadataCore))
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("transcriptionInfo.releaseDate", is(releaseDate)))
                .andExpect(jsonPath("transcriptionInfo.collectionName", is(collectionName)))
                .andExpect(jsonPath("transcriptionInfo.contactName", is(name)))
                .andExpect(jsonPath("transcriptionInfo.contactSurname", is(surname)))
                .andExpect(jsonPath("transcriptionInfo.contactEmail", is(email)))
                .andExpect(jsonPath("transcriptionInfo.description", is(description)))
                .andExpect(jsonPath("transcriptionInfo.documentSrc", is(documentSrc)))
                .andExpect(jsonPath("transcriptionInfo.documentTypes", is(docTypes)))
                .andExpect(jsonPath("transcriptionInfo.guideLines", is(guideLines)))
                .andExpect(jsonPath("transcriptionInfo.imgSrc", is(imgSrc)))

                .andExpect(jsonPath("monographicInfo.title", is(monographicInfoTitle)))
                .andExpect(jsonPath("monographicInfo.biblType.code", is(monographicInfoBiblTypeCode)))
                .andExpect(jsonPath("monographicInfo.actors[0].role.code", is(monographicInfoActor1Role)))
                .andExpect(jsonPath("monographicInfo.actors[0].name", is(monographicInfoActor1Name)))
                .andExpect(jsonPath("monographicInfo.language.code", is(monographicInfoLanguageCode)))
                .andExpect(jsonPath("monographicInfo.chapterNo", is(monographicInfoChapterNo)))
                .andExpect(jsonPath("monographicInfo.volumeNo", is(monographicInfoVolumeNo)))
                .andExpect(jsonPath("monographicInfo.issueNo", is(monographicInfoIssueNo)))
                .andExpect(jsonPath("monographicInfo.pageRange", is(monographicInfoPageRange)))
                .andExpect(jsonPath("monographicInfo.pages", is(monographicInfoPages)))
                .andExpect(jsonPath("monographicInfo.publisher", is(monographicInfoPublisher)))
                .andExpect(jsonPath("monographicInfo.publicationPlace", is(monographicInfoPublicationPlace)))
                .andExpect(jsonPath("monographicInfo.publicationDate", is(monographicInfoPublicationDate)))
                .andExpect(jsonPath("monographicInfo.isbn", is(monographicInfoIsbn)))
                .andExpect(jsonPath("monographicInfo.eisbn", is(monographicInfoEisbn)))
                .andExpect(jsonPath("monographicInfo.issn", is(monographicInfoIssn)))
                .andExpect(jsonPath("monographicInfo.eissn", is(monographicInfoEissn)))
                .andExpect(jsonPath("analyticInfo.name", is("analyticInfoActor1Name")))
                .andExpect(jsonPath("profileDesc.links[0]", is(link1)))
                .andExpect(jsonPath("profileDesc.links[1]", is(link2)))
                .andExpect(jsonPath("profileDesc.summary", is(summary)))
                .andExpect(jsonPath("profileDesc.keywords[0]", is(keyWord1)))
                .andExpect(jsonPath("profileDesc.keywords[1]", is(keyWord2)))
                .andReturn().getResponse().getContentAsString();
        TranscriptionMetadataCore tmc =
                TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, TranscriptionMetadataCore.class);


    }

    @Test
    public void shouldUpdateTranscriptionInfo()
            throws Exception {
        int transcriptionId = 1;

        String releaseDate = "2021-11-11:11:11:11 - 2022-02-2:2:2:2";
        String collectionName = "collection1";
        String name = "Johnnnnnn";
        String surname = "Smithhhhhhh";
        String email = "john_is_a@example.org";
        String description = "Lorem ipsum";
        String documentSrc = "documentSrc";
        String docTypes = "docTypes";
        String guideLines = "guideLines";
        String imgSrc = "imgSrc";
        TranscriptionInfoCore transcriptionInfoCore = new TranscriptionInfoCore();
        transcriptionInfoCore.setReleaseDate(releaseDate);
        transcriptionInfoCore.setCollectionName(collectionName);
        transcriptionInfoCore.setContactName(name);
        transcriptionInfoCore.setContactSurname(surname);
        transcriptionInfoCore.setContactEmail(email);
        transcriptionInfoCore.setDescription(description);
        transcriptionInfoCore.setDocumentSrc(documentSrc);
        transcriptionInfoCore.setDocumentTypes(docTypes);
        transcriptionInfoCore.setGuidelines(guideLines);
        transcriptionInfoCore.setImgSrc(imgSrc);
        String jsonResponse = mvc.perform(
                post("/api/transcriptionss/{transcriptionId}/info", transcriptionId)
                        .header("Authorization", TOKEN)
                        .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(transcriptionInfoCore))
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("releaseDate", is(releaseDate)))
                .andExpect(jsonPath("collectionName", is(collectionName)))
                .andExpect(jsonPath("contactName", is(name)))
                .andExpect(jsonPath("contactSurname", is(surname)))
                .andExpect(jsonPath("contactEmail", is(email)))
                .andExpect(jsonPath("description", is(description)))
                .andExpect(jsonPath("documentSrc", is(documentSrc)))
                .andExpect(jsonPath("documentTypes", is(docTypes)))
                .andExpect(jsonPath("guideLines", is(guideLines)))
                .andExpect(jsonPath("imgSrc", is(imgSrc)))
                .andExpect(jsonPath("hidden", is(true)))
                .andReturn().getResponse().getContentAsString();
        Transcription transcription =
                TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, Transcription.class);


    }


    @Test
    public void shouldNotUpdateTranscriptionWhenLinkIsMalformed()
            throws Exception {
        int transcriptionId = 1;

        TranscriptionCore newTranscription = new TranscriptionCore();

        newTranscription.setAnalyticInfo(getAnalyticInfoCore());
        newTranscription.setMonographicInfo(getMonographicInfoCore());

        ProfileDescCore profileDesc = getProfileDescCore();
        String link1 = "http://pcss.pl";
        String link2 = "hh:ddjd/.dd";
        profileDesc.setLinks(Arrays.asList(link1, link2));
        newTranscription.setProfileDesc(profileDesc);

        mvc.perform(put("/api/transcriptions/{transcriptionId}", transcriptionId).header("Authorization", TOKEN)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(newTranscription))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['profileDesc.links[1]']", is("transcriptions.error.malformedUrl")));
    }

    @Test
    public void shouldUpdateTranscriptionWhenLinkIsLongButMaxLenghtNotExceeded()
            throws Exception {
        int transcriptionId = 1;

        TranscriptionCore newTranscription = new TranscriptionCore();

        newTranscription.setAnalyticInfo(getAnalyticInfoCore());
        newTranscription.setMonographicInfo(getMonographicInfoCore());

        ProfileDescCore profileDesc = getProfileDescCore();
        String link1 =
                "https://longurlmaker.com/go?id=00z04tall126remotehigh0021ShortlinksBeamZtolnkZin1111EzURLf5DecentURLfaraway9Redirx09s5b1u9stowering74g1towering7j18enduringrbxfarZreaching0nstringy5xURlZie18lingering14zganglingRedirxtowering0drawnZout0URLZcoZuk1Smallrhoutstretched9013faraway4TightURLe0o1dprolongedspunZout3803DigBig4lingeringShim20iyXil4hURLPie33DwarfurlwURLcutremotetowering7NanoRefRubyURLm00ShredURLcstretchdloftyenlarged718Ne135TinyLinkm2towering1lengthy7LiteURLXZse5gangling0Shrtnd59tallenduring89yxe4continuedSimURLstretchedm10sDecentURLsustainedlnkZinNotLong1SnipURL409gcURlZiec985gangling4kMetamark4SitelutionsafarZreaching07lengthyfarZreaching04bfarZreaching89easpunZout79elongated2enlarged1lengthycdrunningdeepfarZreachingd1e3r90083q0ShortURLelongated5SimURL06prolonged3a3e6Fly2enduring9bURLHawk001zFwdURL130z10BeamZto1Shim48m6URL0XilShrtnd11sremote0ghaoutstretchedlongish35outstretchedbfarZreachingloftyURLHawkce14652TinyLinklengthened5fstretchedprolonged42WapURLstretchstringy5stringyspunZoutlengthenede1601lengthyhighz9Smallr1elongateURLCutter1d1Shim9distant0afarZoff73rangyURlZiev6Shortlinks7e47e8l6spunZout18ca9DecentURLxexpandedbfFhURL1101elongatestretchzfarawaydistantlnkZin9drawnZout1o731osustainede0stretchingelongatedcontinuedDoioprangybi606URLbgangling187edistant05w03XZse968ciUrlTeaf00NanoRefTinyURL0lShortURLlasting4l8G8L0renlarged7000URLPieCanURL59A2N90longish6URLe10cfaraway8GetShortyNe1rangy08770Xil0e0d49010e5Shim38sustainedcontinued0301URLla684080wlongish6farZreaching09f01RubyURL7afarZreaching1FwdURL1egreatURLPie15prolonged1Minilien6b0942Shim3ShortURLMooURLURlZie41lWapURLdLiteURLu9lanky1continuedlingeringTinyURL0lengthyYepItMooURLUlimitEzURL6NotLong600887beSitelutions8enlargedEasyURL3deepsustainedTraceURL5zSnipURLstretchingspunZoutfarZoffm064150WapURLogreatx0mjexpanded2eURLcut6TinyURLva0DecentURL19DigBigfdq3q060expanded714URLCutter5at12gShorl5URLCutter0TinyLinkoutstretched1talllengthy60c449SHurl1012expandedksustai";
        Assert.assertTrue(link1.length() < 2048 && link1.length() > 256);

        profileDesc.setLinks(Arrays.asList(link1));
        newTranscription.setProfileDesc(profileDesc);

        mvc.perform(put("/api/transcriptions/{transcriptionId}", transcriptionId).header("Authorization", TOKEN)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(newTranscription))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("profileDesc.links[0]", is(link1)));
    }


    @Test
    public void shouldNotCreateOrUpdateTranscriptionWhenLinkIsTooLong()
            throws Exception {

        int transcriptionId = 1;
        String longLink =
                "https://longurlmaker.com/go?id=00z04tall126remotehigh0021ShortlinksBeamZtolnkZin1111EzURLf5DecentURLfaraway9Redirx09s5b1u9stowering74g1towering7j18enduringrbxfarZreaching0nstringy5xURlZie18lingering14zganglingRedirxtowering0drawnZout0URLZcoZuk1Smallrhoutstretched9013faraway4TightURLe0o1dprolongedspunZout3803DigBig4lingeringShim20iyXil4hURLPie33DwarfurlwURLcutremotetowering7NanoRefRubyURLm00ShredURLcstretchdloftyenlarged718Ne135TinyLinkm2towering1lengthy7LiteURLXZse5gangling0Shrtnd59tallenduring89yxe4continuedSimURLstretchedm10sDecentURLsustainedlnkZinNotLong1SnipURL409gcURlZiec985gangling4kMetamark4SitelutionsafarZreaching07lengthyfarZreaching04bfarZreaching89easpunZout79elongated2enlarged1lengthycdrunningdeepfarZreachingd1e3r90083q0ShortURLelongated5SimURL06prolonged3a3e6Fly2enduring9bURLHawk001zFwdURL130z10BeamZto1Shim48m6URL0XilShrtnd11sremote0ghaoutstretchedlongish35outstretchedbfarZreachingloftyURLHawkce14652TinyLinklengthened5fstretchedprolonged42WapURLstretchstringy5stringyspunZoutlengthenede1601lengthyhighz9Smallr1elongateURLCutter1d1Shim9distant0afarZoff73rangyURlZiev6Shortlinks7e47e8l6spunZout18ca9DecentURLxexpandedbfFhURL1101elongatestretchzfarawaydistantlnkZin9drawnZout1o731osustainede0stretchingelongatedcontinuedDoioprangybi606URLbgangling187edistant05w03XZse968ciUrlTeaf00NanoRefTinyURL0lShortURLlasting4l8G8L0renlarged7000URLPieCanURL59A2N90longish6URLe10cfaraway8GetShortyNe1rangy08770Xil0e0d49010e5Shim38sustainedcontinued0301URLla684080wlongish6farZreaching09f01RubyURL7afarZreaching1FwdURL1egreatURLPie15prolonged1Minilien6b0942Shim3ShortURLMooURLURlZie41lWapURLdLiteURLu9lanky1continuedlingeringTinyURL0lengthyYepItMooURLUlimitEzURL6NotLong600887beSitelutions8enlargedEasyURL3deepsustainedTraceURL5zSnipURLstretchingspunZoutfarZoffm064150WapURLogreatx0mjexpanded2eURLcut6TinyURLva0DecentURL19DigBigfdq3q060expanded714URLCutter5at12gShorl5URLCutter0TinyLinkoutstretched1talllengthy60c449SHurl1012expandedksustainedtowering1spunZout11longishUlimitvz4wDoioplanky5celongish0yFly27f0EasyURLn14UlimitbA2NfarZreaching6stretched19DigBig2g970110Star";
        Assert.assertTrue(longLink.length() > 2048);

        TranscriptionCore newTranscription = new TranscriptionCore();
        AnalyticInfoCore analyticInfo = getAnalyticInfoCore();
        MonographicInfoCore monographicInfo = getMonographicInfoCore();
        ProfileDescCore profileDesc = getProfileDescCore();

        newTranscription.setAnalyticInfo(analyticInfo);
        newTranscription.setMonographicInfo(monographicInfo);

        profileDesc.setLinks(Arrays.asList(longLink));
        newTranscription.setProfileDesc(profileDesc);

        mvc.perform(put("/api/transcriptions/{transcriptionId}", transcriptionId).header("Authorization", TOKEN)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(newTranscription))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['profileDesc.links[0]']", is("transcriptions.error.urlTooLong")));
        mvc.perform(post("/api/transcriptions").header("Authorization", TOKEN)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(newTranscription))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['profileDesc.links[0]']", is("transcriptions.error.urlTooLong")));
    }

    private ProfileDescCore getProfileDescCore() {
        //profileDesc
        ProfileDescCore profileDesc = new ProfileDescCore();
        String summary = "summary";
        String keyWord1 = "keyWordUpdated";
        String keyWord2 = "keyWord2";

        profileDesc.setKeywords(Arrays.asList(keyWord1, keyWord2));
        profileDesc.setLinks(new ArrayList<>());
        profileDesc.setSummary(summary);
        return profileDesc;
    }


    private MonographicInfoCore getMonographicInfoCore() {
        //monographicInfo
        MonographicInfoCore monographicInfo = new MonographicInfoCore();

        String monographicInfoTitle = "monographicInfoTitleUpdated";
        String monographicInfoBiblTypeCode = "print";
        String monographicInfoActor1Name = "monographicInfoActor1Name";
        String monographicInfoActor1Role = "author";
        String monographicInfoLanguageCode = "pol";
        Integer monographicInfoChapterNo = 1;
        Integer monographicInfoVolumeNo = 4;
        Integer monographicInfoIssueNo = 3;
        String monographicInfoPageRange = "4-5";
        Integer monographicInfoPages = 5;
        String monographicInfoPublisher = "monographicInfoPublisherUpdated";
        String monographicInfoPublicationPlace = "monographicInfoPublicationPlaceUpdated";
        String monographicInfoPublicationDate = "monographicInfoPublicationDateUpdated";
        String monographicInfoIsbn = "monographicInfoIsbn";
        String monographicInfoEisbn = "monographicInfoEisbn";
        String monographicInfoIssn = "monographicInfoIssn";
        String monographicInfoEissn = "monographicInfoEissn";

        monographicInfo.setTitle(monographicInfoTitle);

        BiblTypeId monographicInfoBiblTypeId = new BiblTypeId();
        monographicInfoBiblTypeId.setCode(monographicInfoBiblTypeCode);
        monographicInfo.setBiblType(monographicInfoBiblTypeId);

        LanguageId monographicInfoLanguage = new LanguageId();
        monographicInfoLanguage.setCode(monographicInfoLanguageCode);
        monographicInfo.setLanguage(monographicInfoLanguage);

        ActorCore monographicInfoActor = new ActorCore();
        monographicInfoActor.setName(monographicInfoActor1Name);
        ActorRoleId monographicInfoActorId = new ActorRoleId();
        monographicInfoActorId.setCode(monographicInfoActor1Role);
        monographicInfoActor.setRole(monographicInfoActorId);
        monographicInfo.setActors(Arrays.asList(monographicInfoActor));

        monographicInfo.setChapterNo(monographicInfoChapterNo);
        monographicInfo.setVolumeNo(monographicInfoVolumeNo);
        monographicInfo.setIssueNo(monographicInfoIssueNo);
        monographicInfo.setPageRange(monographicInfoPageRange);
        monographicInfo.setPages(monographicInfoPages);
        monographicInfo.setPublisher(monographicInfoPublisher);
        monographicInfo.setPublicationPlace(monographicInfoPublicationPlace);
        monographicInfo.setPublicationDate(monographicInfoPublicationDate);
        monographicInfo.setIsbn(monographicInfoIsbn);
        monographicInfo.setEisbn(monographicInfoEisbn);
        monographicInfo.setIssn(monographicInfoIssn);
        monographicInfo.setEissn(monographicInfoEissn);
        return monographicInfo;
    }


    private AnalyticInfoCore getAnalyticInfoCore() {
        //analyticInfo
        AnalyticInfoCore analyticInfo = new AnalyticInfoCore();
        String analyticInfoTitle = "analyticInfoTitleUpdated";
        String analyticInfoLanguageCode = "pol";
        String analyticInfoActor1Name = "analyticInfoActor1Name";
        String analyticInfoActor1RoleCode = "editor";
        analyticInfo.setTitle(analyticInfoTitle);

        LanguageId analyticInfoLanguage = new LanguageId();
        analyticInfoLanguage.setCode(analyticInfoLanguageCode);
        analyticInfo.setLanguage(analyticInfoLanguage);

        ActorCore analyticInfoActor = new ActorCore();
        analyticInfoActor.setName(analyticInfoActor1Name);
        ActorRoleId analyticInfoActor1RoleId = new ActorRoleId();
        analyticInfoActor1RoleId.setCode(analyticInfoActor1RoleCode);
        analyticInfoActor.setRole(analyticInfoActor1RoleId);

        analyticInfo.setActors(Arrays.asList(analyticInfoActor));
        return analyticInfo;
    }

    @Test
    public void shouldNotUpdateTranscriptionWhenNotExists()
            throws Exception {
        int transcriptionId = 999;

        TranscriptionCore newTranscription = new TranscriptionCore();

        mvc.perform(put("/api/transcriptions/{transcriptionId}", transcriptionId).header("Authorization", TOKEN)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(newTranscription))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }


    @Test
    public void shouldAddPagesToTranscription()
            throws Exception {
        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("test1Pdf.pdf");

        MockMultipartFile file1 = new MockMultipartFile("file", "filename11c.pdf", "text/plain", ba);

        int transcriptionId = 1;

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(5)))
                .andExpect(jsonPath("pages.list[0].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[1].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[2].path", is("filename11c.pdf")))
                .andExpect(jsonPath("pages.list[2].thumbnailUrl", endsWith(
                        "iipsrv/iipsrv.fcgi/?IIIF=t1/filename11c_Page0000.tif/pct:0,0,100.0,70.7/!300,300/0/default.jpg")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[3].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[4].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightY", is(3759)));

        MockMultipartFile file2 = new MockMultipartFile("file", "filename11e.pdf", "text/plain", ba);

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file2).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(6)))
                .andExpect(jsonPath("pages.list[0].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[1].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[2].path", is("filename11c.pdf")))
                .andExpect(jsonPath("pages.list[2].thumbnailUrl", endsWith(
                        "iipsrv/iipsrv.fcgi/?IIIF=t1/filename11c_Page0000.tif/pct:0,0,100.0,70.7/!300,300/0/default.jpg")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[3].path", is("filename11e.pdf")))
                .andExpect(jsonPath("pages.list[3].thumbnailUrl", endsWith(
                        "iipsrv/iipsrv.fcgi/?IIIF=t1/filename11e_Page0000.tif/pct:0,0,100.0,70.7/!300,300/0/default.jpg")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[4].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[5].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[5].pageNo", is(6)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightY", is(3759)));

        MockMultipartFile file3 = new MockMultipartFile("file", "filename11b.pdf", "text/plain", ba);

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file3).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(7)))
                .andExpect(jsonPath("pages.list[0].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[1].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[2].path", is("filename11b.pdf")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[3].path", is("filename11c.pdf")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[4].path", is("filename11e.pdf")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[5].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[5].pageNo", is(6)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[6].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[6].pageNo", is(7)))
                .andExpect(jsonPath("pages.list[6].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[6].boundingBox.lowerRightY", is(3759)));

        MockMultipartFile file4 = new MockMultipartFile("file", "filename12a.pdf", "text/plain", ba);

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file4).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(8)))
                .andExpect(jsonPath("pages.list[0].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[1].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[2].path", is("filename11b.pdf")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[3].path", is("filename11c.pdf")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[4].path", is("filename11e.pdf")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[5].path", is("filename12a.pdf")))
                .andExpect(jsonPath("pages.list[5].pageNo", is(6)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[6].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[6].pageNo", is(7)))
                .andExpect(jsonPath("pages.list[6].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[6].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[7].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[7].pageNo", is(8)))
                .andExpect(jsonPath("pages.list[7].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[7].boundingBox.lowerRightY", is(3759)));

        MockMultipartFile file5 = new MockMultipartFile("file", "filename12.pdf", "text/plain", ba);

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file5).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(9)))
                .andExpect(jsonPath("pages.list[0].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[1].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[2].path", is("filename11b.pdf")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[3].path", is("filename11c.pdf")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[4].path", is("filename11e.pdf")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[5].path", is("filename12.pdf")))
                .andExpect(jsonPath("pages.list[5].pageNo", is(6)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[6].path", is("filename12a.pdf")))
                .andExpect(jsonPath("pages.list[6].pageNo", is(7)))
                .andExpect(jsonPath("pages.list[6].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[6].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[6].path", is("filename12a.pdf")))
                .andExpect(jsonPath("pages.list[7].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[7].pageNo", is(8)))
                .andExpect(jsonPath("pages.list[7].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[7].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[8].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[8].pageNo", is(9)))
                .andExpect(jsonPath("pages.list[8].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[8].boundingBox.lowerRightY", is(3759)));
    }

    @Test
    public void shouldAddPageToTranscriptionUsingDjvuFormat()
            throws Exception {
        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("textOnePage.djvu");

        MockMultipartFile file1 = new MockMultipartFile("file", "textOnePage.djvu", "text/plain", ba);

        int transcriptionId = 43;

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(4)))
                .andExpect(jsonPath("pages.list[0].path", is("textOnePage.djvu")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(3508)));

        ba = ResourceFileContentLoader.loadFileContentAsBytes("textMultipage.djvu");
        MockMultipartFile file2 = new MockMultipartFile("file", "textMultipage.djvu", "text/plain", ba);
        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file2).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(7)));

    }


    @Test
    public void shouldAddPageToTranscriptionUsingPNGFormat()
            throws Exception {
        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("test-png.png");

        MockMultipartFile file1 = new MockMultipartFile("file", "test-png.png", "text/plain", ba);

        int transcriptionId = 43;

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(4)))
                .andExpect(jsonPath("pages.list[0].path", is("test-png.png")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(3508)));

    }


    @Test
    public void shouldAddPagesToTranscriptionWhenHasMoreThanOnePage()
            throws Exception {
        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("textMultipage0004t.pdf");

        MockMultipartFile file1 = new MockMultipartFile("file", "filename11c.pdf", "text/plain", ba);

        int transcriptionId = 1;

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(7)))
                .andExpect(jsonPath("pages.list[0].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[1].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[2].path", is("filename11c.pdf")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[3].path", is("filename11c.pdf")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[4].path", is("filename11c.pdf")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[5].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[5].pageNo", is(6)))
                .andExpect(jsonPath("pages.list[6].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[6].pageNo", is(7)));

        MockMultipartFile file2 = new MockMultipartFile("file", "filename11e.pdf", "text/plain", ba);

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file2).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(10)))
                .andExpect(jsonPath("pages.list[0].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[1].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[2].path", is("filename11c.pdf")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[3].path", is("filename11c.pdf")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[4].path", is("filename11c.pdf")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[5].path", is("filename11e.pdf")))
                .andExpect(jsonPath("pages.list[5].pageNo", is(6)))
                .andExpect(jsonPath("pages.list[6].path", is("filename11e.pdf")))
                .andExpect(jsonPath("pages.list[6].pageNo", is(7)))
                .andExpect(jsonPath("pages.list[7].path", is("filename11e.pdf")))
                .andExpect(jsonPath("pages.list[7].pageNo", is(8)))
                .andExpect(jsonPath("pages.list[8].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[8].pageNo", is(9)))
                .andExpect(jsonPath("pages.list[9].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[9].pageNo", is(10)));
    }

    @Test
    public void shouldAddPagesToTranscriptionWhenFilenameContainsWhiteCharacters()
            throws Exception {
        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("file with spaces in name.pdf");

        MockMultipartFile file1 = new MockMultipartFile("file", "file with spaces in name.pdf", "text/plain", ba);

        int transcriptionId = 7;

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(1)))
                .andExpect(jsonPath("pages.list[0].path", is("file_with_spaces_in_name.pdf")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[0].thumbnailUrl", endsWith(
                        "/iipsrv/iipsrv.fcgi/?IIIF=t7/file_with_spaces_in_name_Page0000.tif/pct:0,0,100.0,70.7/!300,300/0/default.jpg")));

    }

    @Test
    public void shouldAddPagesToTranscriptionWhenHasMoreThanOnePageCheckLastPage()
            throws Exception {
        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("multipage_test1.tiff");

        MockMultipartFile file1 = new MockMultipartFile("file", "filename11c.tif", "text/plain", ba);

        int transcriptionId = 1;

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(6)))
                .andExpect(jsonPath("pages.list[0].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[1].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[1].thumbnailUrl", endsWith(
                        "iipsrv/iipsrv.fcgi/?IIIF=t1/page0004v_Page0000.tif/pct:0,0,100.0,66.9/!300,300/0/default.jpg")))
                .andExpect(jsonPath("pages.list[2].path", is("filename11c.tif")))
                .andExpect(jsonPath("pages.list[2].thumbnailUrl", endsWith(
                        "/iipsrv/iipsrv.fcgi/?IIIF=t1/filename11c_Page0001.tif/pct:0,0,100.0,70.7/!300,300/0/default.jpg")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[3].path", is("filename11c.tif")))
                .andExpect(jsonPath("pages.list[3].thumbnailUrl", endsWith(
                        "/iipsrv/iipsrv.fcgi/?IIIF=t1/filename11c_Page0002.tif/pct:0,0,100.0,70.7/!300,300/0/default.jpg")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[4].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[5].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[5].pageNo", is(6)));

    }


    @Test
    public void shouldReturnUserTranscriptionsWithQuery()
            throws Exception {
        String superadminToken = LogInService.getJWTToken(mvc, "superadmin@wp.pl", "superadmin");
        mvc.perform(get("/api/user-transcriptions")
                .param("ownerId", "1")
                .param("q", "Czerwony Kapturek")
                .header("Authorization", superadminToken)
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(2)))
                .andExpect(jsonPath("ownerEmail", is("email2@wp.pl")))
                .andExpect(jsonPath("page", is(1)))
                .andExpect(jsonPath("transcriptions", hasSize(2)))
                .andExpect(jsonPath("transcriptions[0].id", isIn(List.of(1, 8))))
                .andExpect(jsonPath("transcriptions[1].id", isIn(List.of(1, 8))));
    }

    @Test
    public void shouldReturnEmptyListOfUserTranscriptions()
            throws Exception {
        String superadminToken = LogInService.getJWTToken(mvc, "superadmin@wp.pl", "superadmin");
        mvc.perform(get("/api/user-transcriptions")
                .param("ownerId", "1")
                .param("q", "should find nothing")
                .header("Authorization", superadminToken)
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(0)))
                .andExpect(jsonPath("ownerEmail", is("email2@wp.pl")))
                .andExpect(jsonPath("page", is(1)))
                .andExpect(jsonPath("transcriptions", hasSize(0)));
    }

    @Test
    public void shouldAddPagesToTranscriptionWhenMultipartIsZip()
            throws Exception {

        int transcriptionId = 1;

        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("test.zip");

        MockMultipartFile file1 = new MockMultipartFile("file", "test.zip", MediaType.MULTIPART_FORM_DATA_VALUE, ba);

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(9)))
                .andExpect(jsonPath("pages.list[0].path", is("filename0.pdf")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[1].path", is("filename0a.pdf")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[2].path", is("filename1.pdf")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[3].path", is("filename1d.pdf")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[4].path", is("filename3.pdf")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[5].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[5].pageNo", is(6)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[6].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[6].pageNo", is(7)))
                .andExpect(jsonPath("pages.list[6].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[6].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[7].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[7].pageNo", is(8)))
                .andExpect(jsonPath("pages.list[7].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[7].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[8].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[8].pageNo", is(9)))
                .andExpect(jsonPath("pages.list[8].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[8].boundingBox.lowerRightY", is(3759)));
    }


    @Test
    public void shouldAddPagesToTranscriptionWhenMultipartIsMultipagePdfFromJpg()
            throws Exception {

        int transcriptionId = 1;

        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("JpgPdfMultipage2a.pdf");

        MockMultipartFile file1 = new MockMultipartFile("file", "JpgPdfMultipage2a.pdf",
                MediaType.MULTIPART_FORM_DATA_VALUE, ba);

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(8)))
                .andExpect(jsonPath("pages.list[0].path", is("JpgPdfMultipage2a.pdf")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(1969)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(1106)))
                .andExpect(jsonPath("pages.list[1].path", is("JpgPdfMultipage2a.pdf")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightX", is(3188)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightY", is(1594)))
                .andExpect(jsonPath("pages.list[2].path", is("JpgPdfMultipage2a.pdf")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightX", is(3200)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightY", is(2400)))
                .andExpect(jsonPath("pages.list[3].path", is("JpgPdfMultipage2a.pdf")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightX", is(866)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightY", is(569)))
                .andExpect(jsonPath("pages.list[4].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[5].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[5].pageNo", is(6)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[6].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[6].pageNo", is(7)))
                .andExpect(jsonPath("pages.list[6].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[6].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[7].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[7].pageNo", is(8)))
                .andExpect(jsonPath("pages.list[7].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[7].boundingBox.lowerRightY", is(3759)));
    }


    @Test
    public void shouldAddPagesToTranscriptionWhenMultipartIs2Page2Tiff()
            throws Exception {

        int transcriptionId = 1;

        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("multipage_test1.tiff");

        MockMultipartFile file1 = new MockMultipartFile("file", "multipage_test1.tiff",
                MediaType.MULTIPART_FORM_DATA_VALUE, ba);

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(6)))
                .andExpect(jsonPath("pages.list[0].path", is("multipage_test1.tiff")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(650)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(434)))
                .andExpect(jsonPath("pages.list[1].path", is("multipage_test1.tiff")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightX", is(650)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightY", is(434)))
                .andExpect(jsonPath("pages.list[2].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[3].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[4].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[5].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[5].pageNo", is(6)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightY", is(3759)));
    }


    @Test
    public void shouldAddPagesToTranscriptionWhenMultipartIsOnePageTextWithImage()
            throws Exception {

        int transcriptionId = 1;

        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("pageImageOnePage5.pdf");

        MockMultipartFile file1 = new MockMultipartFile("file", "pageImageOnePage5.pdf",
                MediaType.MULTIPART_FORM_DATA_VALUE, ba);

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(5)))
                .andExpect(jsonPath("pages.list[0].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[1].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[2].path", is("pageImageOnePage5.pdf")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[3].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[4].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightY", is(3759)));
    }


    @Test
    public void shouldAddPagesToTranscriptionWhenMultipartIsThreePageTextWithImage()
            throws Exception {

        int transcriptionId = 1;

        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("textImageMultipage5.pdf");

        MockMultipartFile file1 = new MockMultipartFile("file", "textImageMultipage5.pdf",
                MediaType.MULTIPART_FORM_DATA_VALUE, ba);

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(7)))
                .andExpect(jsonPath("pages.list[0].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[1].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[2].path", is("textImageMultipage5.pdf")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[3].path", is("textImageMultipage5.pdf")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[4].path", is("textImageMultipage5.pdf")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[5].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[5].pageNo", is(6)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[6].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[6].pageNo", is(7)))
                .andExpect(jsonPath("pages.list[6].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[6].boundingBox.lowerRightY", is(3759)));
    }


    @Test
    public void shouldAddPagesToTranscriptionWhenMultipartIsThreePageText()
            throws Exception {

        int transcriptionId = 1;

        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("textMultipage0004t.pdf");

        MockMultipartFile file1 = new MockMultipartFile("file", "textMultipage0004t.pdf",
                MediaType.MULTIPART_FORM_DATA_VALUE, ba);

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(7)))
                .andExpect(jsonPath("pages.list[0].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[1].path", is("textMultipage0004t.pdf")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[2].path", is("textMultipage0004t.pdf")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[3].path", is("textMultipage0004t.pdf")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[4].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[5].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[5].pageNo", is(6)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[5].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[6].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[6].pageNo", is(7)))
                .andExpect(jsonPath("pages.list[6].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[6].boundingBox.lowerRightY", is(3759)));
    }


    @Test
    public void shouldAddPagesToTranscriptionWhenMultipartIsOnePageText()
            throws Exception {

        int transcriptionId = 1;

        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("textOnePage.pdf");

        MockMultipartFile file1 = new MockMultipartFile("file", "textOnePage.pdf", MediaType.MULTIPART_FORM_DATA_VALUE,
                ba);

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(5)))
                .andExpect(jsonPath("pages.list[0].path", is("textOnePage.pdf")))
                .andExpect(jsonPath("pages.list[0].pageNo", is(1)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightX", is(2480)))
                .andExpect(jsonPath("pages.list[0].boundingBox.lowerRightY", is(3508)))
                .andExpect(jsonPath("pages.list[1].path", is("page0004r.tif")))
                .andExpect(jsonPath("pages.list[1].pageNo", is(2)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[1].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[2].path", is("page0004v.tif")))
                .andExpect(jsonPath("pages.list[2].pageNo", is(3)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[2].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[3].path", is("page249.tif")))
                .andExpect(jsonPath("pages.list[3].pageNo", is(4)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[3].boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("pages.list[4].path", is("page250.tif")))
                .andExpect(jsonPath("pages.list[4].pageNo", is(5)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("pages.list[4].boundingBox.lowerRightY", is(3759)));
    }


    @Test
    public void shouldThrowQuotaException()
            throws Exception {

        int transcriptionId = 1;

        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("textOnePage.pdf");

        MockMultipartFile file1 = new MockMultipartFile("file", "textOnePage.pdf", MediaType.MULTIPART_FORM_DATA_VALUE,
                ba);

        mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN_FOR_USER_WITH_EXCEEDED_QUOTA)).andExpect(
                status().isBadRequest());
    }

    @Test
    public void shouldNotAddPagesToTranscriptionWhenWrongFileType()
            throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("file", "filename2.txt", "text/plain", "some xml".getBytes());

        int transcriptionId = 1;

        mvc.perform(
                MockMvcRequestBuilders.fileUpload("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                        .file(file1).header("Authorization", TOKEN)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("code", is("pages.error.wrongFileType")))
                .andExpect(jsonPath("params", hasSize(1)))
                .andExpect(jsonPath("params[0]", is("filename2.txt")));
    }


    @Test
    public void shouldNotAddPagesToTranscriptionWhenTranscriptionDoesNotExists()
            throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("file", "filename1c.pdf", "text/plain", "some xml".getBytes());

        int transcriptionId = 999;

        mvc.perform(
                MockMvcRequestBuilders.fileUpload("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                        .file(file1).header("Authorization", TOKEN)).andExpect(status().isNotFound());
    }


    @Test
    public void shouldReturnProperTranscriptionPermissions()
            throws Exception {
        int transcriptionId = 1;
        mvc.perform(get("/api/transcriptions/{transcriptionId}/permissions", transcriptionId)
                .header("Authorization", TOKEN)).andExpect(status().isOk())

                .andExpect(jsonPath("publiclyAvailable", is(true)))
                .andExpect(jsonPath("allowedUsers", hasSize(4)))
                .andExpect(jsonPath("allowedUsers[0].user.email", is("email2@wp.pl")))
                .andExpect(jsonPath("allowedUsers[0].role.code", is("admin")))
                .andExpect(jsonPath("allowedUsers[1].user.email", is("email@wp.pl")))
                .andExpect(jsonPath("allowedUsers[1].role.code", is("admin")))
                .andExpect(jsonPath("allowedUsers[2].user.email", is("transcriber@wp.pl")))
                .andExpect(jsonPath("allowedUsers[2].role.code", is("contributor")))
                .andExpect(jsonPath("allowedUsers[3].user.email", is("email1@wp.pl")))
                .andExpect(jsonPath("allowedUsers[3].role.code", is("contributor")))
                .andExpect(jsonPath("allowedGroups", hasSize(1)))
                .andExpect(jsonPath("allowedGroups[0].group.name", is("groupName")));
    }


    @Test
    public void shouldReturnNotFoundException()
            throws Exception {
        int transcriptionId = 9999;
        mvc.perform(get("/api/transcriptions/{transcriptionId}/permissions", transcriptionId)
                .header("Authorization", TOKEN)).andExpect(status().isNotFound());
    }


    @Test
    public void shouldReturnUnauthorizedException()
            throws Exception {
        int transcriptionId = 1;
        mvc.perform(get("/api/transcriptions/{transcriptionId}/permissions", transcriptionId))
                .andExpect(status().isUnauthorized());
    }


    @Test
    public void shouldMakeTranscriptionHidden()
            throws Exception {

        int transcriptionId = 1;

        mvc.perform(put("/api/transcriptions/{transcriptionId}/hidden", transcriptionId).content("true")
                .contentType(MediaType.APPLICATION_JSON_VALUE).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("hidden", is(true)));
    }


    @Test
    public void shouldMakeTranscriptionNotHidden()
            throws Exception {

        int transcriptionId = 1;

        mvc.perform(put("/api/transcriptions/{transcriptionId}/hidden", transcriptionId).content("true")
                .contentType(MediaType.APPLICATION_JSON_VALUE).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("hidden", is(true)));

        mvc.perform(put("/api/transcriptions/{transcriptionId}/hidden", transcriptionId).content("false")
                .contentType(MediaType.APPLICATION_JSON_VALUE).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("hidden", is(false)));
    }


    @Test
    public void shouldReturnAccessDeniedExceptionForHiddenFlag()
            throws Exception {
        int transcriptionId = 1;

        mvc.perform(put("/api/transcriptions/{transcriptionId}/hidden", transcriptionId).content("true")
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isUnauthorized());

    }


    @Test
    public void shouldReturnUnauthorizedExceptionForHiddenFlag()
            throws Exception {
        int transcriptionId = 1;

        mvc.perform(put("/api/transcriptions/{transcriptionId}/hidden", transcriptionId).content("true")
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isUnauthorized());

    }


    @Test
    public void shouldDeleteTranscriptionWhenUserIsSuperadmin()
            throws Exception {
        long transcriptionId = 5L;


        String token = LogInService.getJWTToken(mvc, "superadmin@wp.pl", "superadmin");

        mvc.perform(delete("/api/transcriptions/{transcriptionId}", transcriptionId).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }


    @Test
    public void shouldNotDeleteTranscriptionWhenUserHasNoPermission()
            throws Exception {
        long transcriptionId = 5L;


        mvc.perform(delete("/api/transcriptions/{transcriptionId}", transcriptionId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }


    @Test
    public void shouldReturnDefaultLanguageWhenThereWereOcrMethodInvocation()
            throws Exception {
        long transcriptionId = 2L;
        long pageId = 5L;

        mvc.perform(
                put("/api/transcriptions/{transcriptionId}/pages/{pageId}/recognize", transcriptionId, pageId).param(
                        "lang", "eng").param("ocrMode", OcrMode.OVERRIDE.name())
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        mvc.perform(get("/api/transcriptions/{transcriptionId}/default-language", transcriptionId)
                .contentType(MediaType.APPLICATION_JSON).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("$", is("eng")));
    }


    @Test
    public void shouldReturnDefaultLangWhenThereWereNoOcrMethodInvocationAndHasAnalyticalInfo()
            throws Exception {
        long transcriptionId = 1L;

        mvc.perform(get("/api/transcriptions/{transcriptionId}/default-language", transcriptionId)
                .contentType(MediaType.APPLICATION_JSON).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("$", is("pol")));
    }


    @Test
    public void shouldReturnDefaultLangWhenThereWereNoOcrMethodInvocationAndHasNoAnalyticalInfo()
            throws Exception {
        long transcriptionId = 8L;

        mvc.perform(get("/api/transcriptions/{transcriptionId}/default-language", transcriptionId)
                .contentType(MediaType.APPLICATION_JSON).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("$", is("eng")));
    }


    @Test
    public void shouldReturnDefaultLangWhenThereWereNoOcrMethodInvocationAndHasNoAnalyticalInfoAndNoMonographicInfo()
            throws Exception {
        long transcriptionId = 7L;

        mvc.perform(get("/api/transcriptions/{transcriptionId}/default-language", transcriptionId)
                .contentType(MediaType.APPLICATION_JSON).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("$", is("pol")));
    }


    @Test
    public void shouldNotDeleteTranscriptionWhenTranscriptionDoesNotExists()
            throws Exception {
        long transcriptionId = 999L;


        mvc.perform(delete("/api/transcriptions/{transcriptionId}", transcriptionId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnTranscriptionContentAsTeiZip() throws Exception {

        Long transcriptionId = 2L;

        MvcResult resultInit = mvc.perform(
                get("/api/transcriptions/{transcriptionId}/content", transcriptionId)
                        .param("type", "tei")
                        .param("zip", "true")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk()).andReturn();

        MvcResult result = mvc.perform(asyncDispatch(resultInit))
                .andExpect(status().isOk()).andReturn();

        ArrayList<String> pagesNames = new ArrayList<>();
        ArrayList<String> pagesContents = new ArrayList<>();
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                result.getResponse().getContentAsByteArray())) {
            ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);
            ZipEntry entry = null;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                //read content
                StringBuilder sb = new StringBuilder();
                Scanner sc = new Scanner(zipInputStream);
                while (sc.hasNextLine()) {
                    sb.append(sc.nextLine());
                }
                pagesNames.add(entry.getName());
                pagesContents.add(sb.toString());
            }
            zipInputStream.close();
        }

        //check response
        assertThat(pagesNames.size(), is(2));
        assertThat(pagesContents.size(), is(2));
        assertThat(pagesNames.get(0), is("Image5.tei"));
        assertThat(pagesNames.get(1), is("Image6.tei"));
        assertThat(pagesContents.get(0),
                containsString("<surface id=\"page_1\" ulx=\"0\" uly=\"0\" lrx=\"2706\" lry=\"4042\">"));
        assertThat(pagesContents.get(1),
                containsString("<surface id=\"page_2\" ulx=\"0\" uly=\"0\" lrx=\"2837\" lry=\"4035\">"));
    }

    @Test
    public void shouldReturnTeiFileForTranscription() throws Exception {

        Long transcriptionId = 2L;

        MvcResult resultInit = mvc.perform(
                get("/api/transcriptions/{transcriptionId}/content", transcriptionId)
                        .param("type", "tei")
                        .param("zip", "false")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk()).andReturn();

        MvcResult result = mvc.perform(asyncDispatch(resultInit))
                .andExpect(status().isOk()).andReturn();

        String teiContent = result.getResponse().getContentAsString();
        assertThat(teiContent, is("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\n" +
                "<TEI>\n" +
                "  <teiHeader>\n" +
                "    <fileDesc>\n" +
                "      <sourceDesc>\n" +
                "        <biblStruct>\n" +
                "          <analytic>\n" +
                "            <title>Lalka</title>\n" +
                "            <textLang mainLang=\"eng\"/>\n" +
                "            <author id=\"6\">\n" +
                "              <persName>Bolesław Prus</persName>\n" +
                "            </author>\n" +
                "          </analytic>\n" +
                "          <monogr>\n" +
                "            <title>Some title</title>\n" +
                "          </monogr>\n" +
                "          <imprint/>\n" +
                "        </biblStruct>\n" +
                "        <msDesc>\n" +
                "          <msIdentifier>\n" +
                "            <repositiory>VTL-REPO</repositiory>\n" +
                "            <idno>VTL-T2</idno>\n" +
                "          </msIdentifier>\n" +
                "        </msDesc>\n" +
                "      </sourceDesc>\n" +
                "      <titleStmt>\n" +
                "        <title level=\"a\">Lalka</title>\n" +
                "      </titleStmt>\n" +
                "      <titleStmt>\n" +
                "        <title level=\"m\">Some title</title>\n" +
                "      </titleStmt>\n" +
                "    </fileDesc>\n" +
                "    <profileDesc>\n" +
                "      <textClass>\n" +
                "        <keywords>\n" +
                "          <term>koń</term>\n" +
                "          <term>słoń</term>\n" +
                "        </keywords>\n" +
                "      </textClass>\n" +
                "      <abstract>some summary</abstract>\n" +
                "    </profileDesc>\n" +
                "  </teiHeader>\n" +
                "  <sourceDoc>\n" +
                "    <surface id=\"page_1\" ulx=\"0\" uly=\"0\" lrx=\"2706\" lry=\"4042\">\n" +
                "      <zone type=\"list\">\n" +
                "        <zone ulx=\"1\" uly=\"1\" lrx=\"9\" lry=\"10\" type=\"item\">\n" +
                "          <line ulx=\"1\" uly=\"1\" lrx=\"9\" lry=\"10\">Ala ma kota6</line>\n" +
                "        </zone>\n" +
                "        <zone ulx=\"1\" uly=\"1\" lrx=\"9\" lry=\"10\" type=\"item\">\n" +
                "          <line ulx=\"1\" uly=\"1\" lrx=\"9\" lry=\"10\">Ala ma kota7</line>\n" +
                "        </zone>\n" +
                "        <zone ulx=\"1\" uly=\"1\" lrx=\"9\" lry=\"10\" type=\"item\">\n" +
                "          <line ulx=\"1\" uly=\"1\" lrx=\"9\" lry=\"10\">Ala ma kota8</line>\n" +
                "        </zone>\n" +
                "        <zone ulx=\"1\" uly=\"1\" lrx=\"9\" lry=\"10\" type=\"item\">\n" +
                "          <line ulx=\"1\" uly=\"1\" lrx=\"9\" lry=\"10\">Ala ma kota9</line>\n" +
                "        </zone>\n" +
                "      </zone>\n" +
                "      <zone type=\"list\">\n" +
                "        <zone ulx=\"393\" uly=\"806\" lrx=\"2054\" lry=\"863\" type=\"item\"/>\n" +
                "        <zone ulx=\"403\" uly=\"905\" lrx=\"2057\" lry=\"976\" type=\"item\"/>\n" +
                "      </zone>\n" +
                "      <zone type=\"list\">\n" +
                "        <zone ulx=\"631\" uly=\"1076\" lrx=\"1843\" lry=\"1125\" type=\"item\"/>\n" +
                "        <zone ulx=\"312\" uly=\"1276\" lrx=\"2132\" lry=\"1335\" type=\"item\"/>\n" +
                "      </zone>\n" +
                "    </surface>\n" +
                "    <surface id=\"page_2\" ulx=\"0\" uly=\"0\" lrx=\"2837\" lry=\"4035\">\n" +
                "      <zone type=\"list\">\n" +
                "        <zone ulx=\"1\" uly=\"1\" lrx=\"9\" lry=\"10\" type=\"item\">\n" +
                "          <line ulx=\"1\" uly=\"1\" lrx=\"9\" lry=\"10\">Ala ma kota9</line>\n" +
                "        </zone>\n" +
                "        <zone ulx=\"1\" uly=\"1\" lrx=\"9\" lry=\"10\" type=\"item\">\n" +
                "          <line ulx=\"1\" uly=\"1\" lrx=\"9\" lry=\"10\">Ala ma kota9</line>\n" +
                "        </zone>\n" +
                "        <zone ulx=\"1\" uly=\"1\" lrx=\"9\" lry=\"10\" type=\"item\">\n" +
                "          <line ulx=\"1\" uly=\"1\" lrx=\"9\" lry=\"10\">Ala ma kota9</line>\n" +
                "        </zone>\n" +
                "      </zone>\n" +
                "    </surface>\n" +
                "  </sourceDoc>\n" +
                "  <text>\n" +
                "    <body>\n" +
                "      <div type=\"VTL\">\n" +
                "        <list>\n" +
                "          <item>Ala ma kota6</item>\n" +
                "          <item>Ala ma kota7</item>\n" +
                "          <item>Ala ma kota8</item>\n" +
                "          <item>Ala ma kota9</item>\n" +
                "        </list>\n" +
                "        <list>\n" +
                "          <item/>\n" +
                "          <item/>\n" +
                "        </list>\n" +
                "        <list>\n" +
                "          <item/>\n" +
                "          <item/>\n" +
                "        </list>\n" +
                "        <list>\n" +
                "          <item>Ala ma kota9</item>\n" +
                "          <item>Ala ma kota9</item>\n" +
                "          <item>Ala ma kota9</item>\n" +
                "        </list>\n" +
                "      </div>\n" +
                "    </body>\n" +
                "  </text>\n" +
                "</TEI>\n"));
    }

    @Test
    public void shouldReturnTranscriptionContentAsHocrZip() throws Exception {

        Long transcriptionId = 2L;

        MvcResult resultInit = mvc.perform(
                get("/api/transcriptions/{transcriptionId}/content", transcriptionId)
                        .param("type", "hocr")
                        .param("zip", "true")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk()).andReturn();

        MvcResult result = mvc.perform(asyncDispatch(resultInit))
                .andExpect(status().isOk()).andReturn();

        ArrayList<String> pagesNames = new ArrayList<>();
        ArrayList<String> pagesContents = new ArrayList<>();
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                result.getResponse().getContentAsByteArray())) {
            ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);
            ZipEntry entry = null;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                //read content
                StringBuilder sb = new StringBuilder();
                Scanner sc = new Scanner(zipInputStream);
                while (sc.hasNextLine()) {
                    sb.append(sc.nextLine());
                }
                pagesNames.add(entry.getName());
                pagesContents.add(sb.toString());
            }
            zipInputStream.close();
        }

        //check response
        assertThat(pagesNames.size(), is(2));
        assertThat(pagesContents.size(), is(2));
        assertThat(pagesNames.get(0), is("Image5.hocr"));
        assertThat(pagesNames.get(1), is("Image6.hocr"));
        assertThat(pagesContents.get(0), containsString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertThat(pagesContents.get(0), containsString("Ala ma kota8"));
        assertThat(pagesContents.get(1), containsString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertThat(pagesContents.get(1), containsString("Ala ma kota9"));
    }

    @Test
    public void shouldReturnTranscriptionContentAsTextInZip() throws Exception {

        Long transcriptionId = 2L;

        MvcResult resultInit = mvc.perform(
                get("/api/transcriptions/{transcriptionId}/content", transcriptionId)
                        .param("type", "plain_text")
                        .param("zip", "true")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk()).andReturn();

        MvcResult result = mvc.perform(asyncDispatch(resultInit))
                .andExpect(status().isOk()).andReturn();

        ArrayList<String> pagesNames = new ArrayList<>();
        ArrayList<String> pagesContents = new ArrayList<>();
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                result.getResponse().getContentAsByteArray())) {
            ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);
            ZipEntry entry = null;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                //read content
                StringBuilder sb = new StringBuilder();
                Scanner sc = new Scanner(zipInputStream);
                while (sc.hasNextLine()) {
                    sb.append(sc.nextLine());
                }
                pagesNames.add(entry.getName());
                pagesContents.add(sb.toString());
            }
            zipInputStream.close();
        }

        //check response
        assertThat(pagesNames.size(), is(2));
        assertThat(pagesContents.size(), is(2));
        assertThat(pagesNames.get(0), is("Image5.txt"));
        assertThat(pagesNames.get(1), is("Image6.txt"));
        assertThat(pagesContents.get(0), containsString("Ala ma kota8"));
        assertThat(pagesContents.get(1), containsString("Ala ma kota9"));
    }

    @Test
    public void shouldReturnTranscriptionContentAsOneFile() throws Exception {

        Long transcriptionId = 2L;

        MvcResult resultInit = mvc.perform(
                get("/api/transcriptions/{transcriptionId}/content", transcriptionId)
                        .param("type", "plain_text")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk()).andReturn();

        MvcResult result = mvc.perform(asyncDispatch(resultInit))
                .andExpect(status().isOk()).andReturn();

        String textContent = result.getResponse().getContentAsString();
        assertThat(textContent, containsString("Ala ma kota6"));
        assertThat(textContent, containsString("Ala ma kota7"));
        assertThat(textContent, containsString("Ala ma kota8"));
        assertThat(textContent, containsString("Ala ma kota9"));
    }

    @Test
    public void shouldReturnIndexesForTranscription() throws Exception {

        IndexCore indexCore = new IndexCore();
        indexCore.setIndexType(IndexType.PERSON);
        indexCore.setName("Test");
        indexCore.setAccessibleOn("https://www.example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("type", is(IndexType.PERSON.getValue())))
                .andExpect(jsonPath("name", is("Test")))
                .andExpect(jsonPath("accessibleOn", nullValue()));

        System.out.println(mvc.perform(put("/api/indexes/{indexId}/{verseId}", 1L, 1L)
                .header("Authorization", TOKEN)).andReturn().getResponse().getContentAsString());

        mvc.perform(get("/api/indexes/").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test")));

        Long transcriptionId = 1L;

        mvc.perform(get("/api/transcriptions/{transcriptionId}/indexes", transcriptionId)
                .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(indexCore.getName())))
                .andReturn();

    }

    @Test
    public void shouldReturnIndexesFromDifferentPagesForOneTranscription() throws Exception {

        //Page 1
        IndexCore indexCore = new IndexCore();
        indexCore.setIndexType(IndexType.PERSON);
        indexCore.setName("Test");
        indexCore.setAccessibleOn("https://www.example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("type", is(IndexType.PERSON.getValue())))
                .andExpect(jsonPath("name", is("Test")))
                .andExpect(jsonPath("accessibleOn", nullValue()));

        mvc.perform(put("/api/indexes/{indexId}/{verseId}", 1L, 1L)
                .header("Authorization", TOKEN));

        mvc.perform(get("/api/indexes/").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test")));

        //Page 2
        IndexCore indexCore2 = new IndexCore();
        indexCore2.setIndexType(IndexType.PLACE);
        indexCore2.setName("Test2");
        indexCore2.setAccessibleOn("https://www.example2.com");

        String payload2 = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore2);
        log.debug("JSON: " + payload2);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload2)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("type", is(IndexType.PLACE.getValue())))
                .andExpect(jsonPath("name", is("Test2")))
                .andExpect(jsonPath("accessibleOn", is(indexCore2.getAccessibleOn())));

        mvc.perform(put("/api/indexes/{indexId}/{verseId}", 2L, 16L)
                .header("Authorization", TOKEN));

        mvc.perform(get("/api/indexes/").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(indexCore.getName())))
                .andExpect(jsonPath("$[1].name", is(indexCore2.getName())));

        Long transcriptionId = 1L;

        mvc.perform(get("/api/transcriptions/{transcriptionId}/indexes", transcriptionId)
                .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(indexCore.getName())));

    }


    @Test
    public void shouldNotReturnIndexesFromOtherTranscriptions() throws Exception {

        //Transcription 1 - Page 1
        IndexCore indexCore = new IndexCore();
        indexCore.setIndexType(IndexType.PERSON);
        indexCore.setName("Test");
        indexCore.setAccessibleOn("https://www.example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue())).andExpect(jsonPath("type", is(IndexType.PERSON.getValue())))
                .andExpect(jsonPath("name", is("Test")))
                .andExpect(jsonPath("accessibleOn", nullValue()));

        mvc.perform(put("/api/indexes/{indexId}/{verseId}", 1L, 1L)
                .header("Authorization", TOKEN));

        mvc.perform(get("/api/indexes/").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test")));

        //Transcription 1 - Page 2
        IndexCore indexCore2 = new IndexCore();
        indexCore2.setIndexType(IndexType.PLACE);
        indexCore2.setName("Test2");
        indexCore2.setAccessibleOn("https://www.example2.com");

        String payload2 = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore2);
        log.debug("JSON: " + payload2);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload2)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("type", is(IndexType.PLACE.getValue())))
                .andExpect(jsonPath("name", is("Test2")))
                .andExpect(jsonPath("accessibleOn", is(indexCore2.getAccessibleOn())));

        mvc.perform(put("/api/indexes/{indexId}/{verseId}", 2L, 16L)
                .header("Authorization", TOKEN));

        //Transcription 2 - Page 5
        IndexCore indexCore3 = new IndexCore();
        indexCore3.setIndexType(IndexType.PLACE);
        indexCore3.setName("Test3");
        indexCore3.setAccessibleOn("https://www.example3.com");

        String payload3 = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore3);
        log.debug("JSON: " + payload3);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload3)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue())).andExpect(jsonPath("type", is(IndexType.PLACE.getValue())))
                .andExpect(jsonPath("name", is("Test3")))
                .andExpect(jsonPath("accessibleOn", is(indexCore3.getAccessibleOn())));

        mvc.perform(put("/api/indexes/{indexId}/{verseId}", 3L, 62L)
                .header("Authorization", TOKEN));

        mvc.perform(get("/api/indexes/").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name", is(indexCore.getName())))
                .andExpect(jsonPath("$[1].name", is(indexCore2.getName())))
                .andExpect(jsonPath("$[2].name", is(indexCore3.getName())));

        Long transcriptionId = 1L;

        mvc.perform(get("/api/transcriptions/{transcriptionId}/indexes", transcriptionId)
                .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(indexCore.getName())))
                .andExpect(jsonPath("$[1].name", is(indexCore2.getName())));

    }

    //TODO - check case sensitivity
    @Test
    public void shouldSearchForIndexesFromDifferentPagesForOneTranscriptionAndType() throws Exception {

        //Page 1
        IndexCore indexCore = new IndexCore();
        indexCore.setIndexType(IndexType.PERSON);
        indexCore.setName("Test");
        indexCore.setAccessibleOn("https://www.example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("type", is(IndexType.PERSON.getValue())))
                .andExpect(jsonPath("name", is("Test")))
                .andExpect(jsonPath("accessibleOn", nullValue()));

        mvc.perform(put("/api/indexes/{indexId}/{verseId}", 1L, 1L)
                .header("Authorization", TOKEN));

        mvc.perform(get("/api/indexes/").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test")));

        //Page 2
        IndexCore indexCore2 = new IndexCore();
        indexCore2.setIndexType(IndexType.PLACE);
        indexCore2.setName("Test");
        indexCore2.setAccessibleOn("https://www.example2.com");

        String payload2 = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore2);
        log.debug("JSON: " + payload2);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload2)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("type", is(IndexType.PLACE.getValue())))
                .andExpect(jsonPath("name", is("Test")))
                .andExpect(jsonPath("accessibleOn", is(indexCore2.getAccessibleOn())));

        mvc.perform(put("/api/indexes/{indexId}/{verseId}", 2L, 16L)
                .header("Authorization", TOKEN));

        mvc.perform(get("/api/indexes/").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(indexCore.getName())))
                .andExpect(jsonPath("$[1].name", is(indexCore2.getName())));

        Long transcriptionId = 1L;

        mvc.perform(get("/api/transcriptions/{transcriptionId}/indexes/search", transcriptionId)
                .header("Authorization", TOKEN)
                .param("searchPhrase", "Test")
                .param("type", "PLACE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(indexCore2.getName())))
                .andExpect(jsonPath("$[0].type", is(indexCore2.getIndexType().toString())));

        mvc.perform(get("/api/transcriptions/{transcriptionId}/indexes/search", transcriptionId)
                .header("Authorization", TOKEN)
                .param("searchPhrase", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(indexCore.getName())))
                .andExpect(jsonPath("$[0].type", is(indexCore.getIndexType().toString())))
                .andExpect(jsonPath("$[1].name", is(indexCore2.getName())))
                .andExpect(jsonPath("$[1].type", is(indexCore2.getIndexType().toString())));

    }


    @Test
    public void shouldCreateLayerAndPageLayers() throws Exception {

        Long transcriptionId = 1L;

        LayerCreationDto layerCreationDto = new LayerCreationDto("name", "description", LayerCreationDto.COPY.NONE,
                null);

        mvc.perform(post("/api/transcriptions/{transcriptionId}/layer", transcriptionId)

                        .header("Authorization", TOKEN)
                        .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(layerCreationDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("name", is( layerCreationDto.getName())))
                .andExpect(jsonPath("description", is( layerCreationDto.getDescription())));

        mvc.perform(get("/api/transcriptions/{transcriptionId}/layer", transcriptionId)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void shouldGetAllLayersForTranscription() throws Exception {

        Long transcriptionId = 1L;

        LayerCore layerCore = new LayerCore();
        layerCore.setTranscriptionId(transcriptionId);
        layerCore.setDescription("test");
        layerCore.setName("test");

        mvc.perform(post("/api/transcriptions/{transcriptionId}/layer", transcriptionId)

                        .header("Authorization", TOKEN)
                        .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(layerCore))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("name", is( layerCore.getName())))
                .andExpect(jsonPath("description", is( layerCore.getDescription())));

        LayerCore layerCore2 = new LayerCore();
        layerCore2.setTranscriptionId(transcriptionId);
        layerCore2.setDescription("test2");
        layerCore2.setName("test2");

        mvc.perform(post("/api/transcriptions/{transcriptionId}/layer", transcriptionId)

                        .header("Authorization", TOKEN)
                        .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(layerCore2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("name", is( layerCore2.getName())))
                .andExpect(jsonPath("description", is( layerCore2.getDescription())));

        mvc.perform(get("/api/transcriptions/{transcriptionId}/layer", transcriptionId)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is( layerCore.getName())))
                .andExpect(jsonPath("$[0].description", is( layerCore.getDescription())))
                .andExpect(jsonPath("$[1].name", is( layerCore2.getName())))
                .andExpect(jsonPath("$[1].description", is( layerCore2.getDescription())));
    }

    @Test
    public void shouldDeleteLayersForTranscription() throws Exception {

        Long transcriptionId = 1L;

        LayerCore layerCore = new LayerCore();
        layerCore.setTranscriptionId(transcriptionId);
        layerCore.setDescription("test");
        layerCore.setName("test");

        mvc.perform(post("/api/transcriptions/{transcriptionId}/layer", transcriptionId)

                        .header("Authorization", TOKEN)
                        .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(layerCore))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("name", is( layerCore.getName())))
                .andExpect(jsonPath("description", is( layerCore.getDescription())));

        LayerCore layerCore2 = new LayerCore();
        layerCore2.setTranscriptionId(transcriptionId);
        layerCore2.setDescription("test2");
        layerCore2.setName("test2");

        String jsonResponse = mvc.perform(post("/api/transcriptions/{transcriptionId}/layer", transcriptionId)

                        .header("Authorization", TOKEN)
                        .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(layerCore2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("name", is( layerCore2.getName())))
                .andExpect(jsonPath("description", is( layerCore2.getDescription())))
                .andReturn().getResponse().getContentAsString();

        Long layerId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, LayerDto.class).getId();

        mvc.perform(get("/api/transcriptions/{transcriptionId}/layer", transcriptionId)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is( layerCore.getName())))
                .andExpect(jsonPath("$[0].description", is( layerCore.getDescription())))
                .andExpect(jsonPath("$[1].id", is(layerId.intValue())))
                .andExpect(jsonPath("$[1].name", is( layerCore2.getName())))
                .andExpect(jsonPath("$[1].description", is( layerCore2.getDescription())));

        /*
        mvc.perform(get("/api/transcriptions/{transcriptionId}/layer/{layerId}/pagelayers", transcriptionId, layerId )
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
*/
        mvc.perform(get("/api/pageLayers/layer/{layerId}", layerId )
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        mvc.perform(delete("/api/transcriptions/{transcriptionId}/layer/{layerId}", transcriptionId, layerId )
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());

        mvc.perform(get("/api/pageLayers/layer/{layerId}", layerId )
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));


        mvc.perform(get("/api/transcriptions/{transcriptionId}/layer", transcriptionId)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is( layerCore.getName())))
                .andExpect(jsonPath("$[0].description", is( layerCore.getDescription())));

    }


    @Test
    public void shouldGetPageLayersForLayer() throws Exception {

        Long transcriptionId = 1L;

        LayerCore layerCore = new LayerCore();
        layerCore.setTranscriptionId(transcriptionId);
        layerCore.setDescription("test");
        layerCore.setName("test");

        String jsonResponse = mvc.perform(post("/api/transcriptions/{transcriptionId}/layer", transcriptionId)
                        .header("Authorization", TOKEN)
                        .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(layerCore))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("name", is( layerCore.getName())))
                .andExpect(jsonPath("description", is( layerCore.getDescription())))
                .andReturn().getResponse().getContentAsString();

        Long layerId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, LayerDto.class).getId();

        mvc.perform(get("/api/transcriptions/{transcriptionId}/layer", transcriptionId)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is( layerCore.getName())))
                .andExpect(jsonPath("$[0].description", is( layerCore.getDescription())));

        mvc.perform(get("/api/pageLayers/layer/{layerId}", layerId )
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

    }

    @Test
    public void shouldUpdateLayer() throws Exception {

        Long transcriptionId = 1L;

        LayerCore layerCore = new LayerCore();
        layerCore.setTranscriptionId(transcriptionId);
        layerCore.setDescription("test");
        layerCore.setName("test");

        String jsonResponse = mvc.perform(post("/api/transcriptions/{transcriptionId}/layer", transcriptionId)

                        .header("Authorization", TOKEN)
                        .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(layerCore))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("name", is( layerCore.getName())))
                .andExpect(jsonPath("description", is( layerCore.getDescription())))
                .andReturn().getResponse().getContentAsString();

        Long layerId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, LayerDto.class).getId();

        LayerCore updateLayerCore = new LayerCore();
        updateLayerCore.setTranscriptionId(transcriptionId);
        updateLayerCore.setDescription("test update");
        updateLayerCore.setName("test update");


        mvc.perform(put("/api/transcriptions/{transcriptionId}/layer/{layerId}", transcriptionId, layerId)
                        .header("Authorization", TOKEN)
                        .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(updateLayerCore))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(layerId.intValue())))
                .andExpect(jsonPath("name", is( updateLayerCore.getName())))
                .andExpect(jsonPath("description", is( updateLayerCore.getDescription())));

    }
}
