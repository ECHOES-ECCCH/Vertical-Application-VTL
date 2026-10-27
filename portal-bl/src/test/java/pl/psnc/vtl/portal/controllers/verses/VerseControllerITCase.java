package pl.psnc.vtl.portal.controllers.verses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.conf.TestJsonMapper;
import pl.psnc.vtl.portal.dto.layers.LayerCore;
import pl.psnc.vtl.portal.dto.layers.LayerDto;
import pl.psnc.vtl.portal.dto.regions.BoundingBoxCore;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.verses.VerseStatus;

import javax.transaction.Transactional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
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
public class VerseControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String TOKEN;

    ObjectMapper objectMapper = new ObjectMapper();

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";


    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
        objectMapper.registerModule(new JavaTimeModule());
    }


    @Test
    public void shouldAddVerse()
            throws Exception {
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
                .andExpect(jsonPath("name", is(layerCore.getName())))
                .andExpect(jsonPath("description", is(layerCore.getDescription())))
                .andReturn().getResponse().getContentAsString();

        Long layerId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, LayerDto.class).getId();

        int pageId = 1;

        Integer upperLeftX = 70;
        Integer upperLeftY = 1202;
        Integer lowerRightX = 1100;
        Integer lowerRightY = 1360;

        BoundingBoxCore boundingBoxCore = new BoundingBoxCore();
        boundingBoxCore.setUpperLeftX(upperLeftX);
        boundingBoxCore.setUpperLeftY(upperLeftY);
        boundingBoxCore.setLowerRightX(lowerRightX);
        boundingBoxCore.setLowerRightY(lowerRightY);

        mvc.perform(post("/api/verses/{pageId}", pageId)
                        .param("layerId", layerId.toString())
                        .header("Authorization", TOKEN)
                        .content(objectMapper.writeValueAsString(boundingBoxCore)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[0].active", is(false))).andExpect(jsonPath("$[1].active", is(false)))
                .andExpect(jsonPath("$[2].active", is(true)))
                .andExpect(jsonPath("$[2].boundingBox.upperLeftX", is(upperLeftX)))
                .andExpect(jsonPath("$[2].boundingBox.upperLeftY", is(upperLeftY)))
                .andExpect(jsonPath("$[2].boundingBox.lowerRightX", is(lowerRightX)))
                .andExpect(jsonPath("$[2].boundingBox.lowerRightY", is(lowerRightY)))
                .andExpect(jsonPath("$[3].active", is(false))).andExpect(jsonPath("$[4].active", is(false)))
                .andExpect(jsonPath("$[5].active", is(false))).andExpect(jsonPath("$[6].active", is(false)));
    }


    @Test
    public void shouldNotAddVerseWhenPageDoNotExists()
            throws Exception {
        int pageId = 1111;

        Integer upperLeftX = 1;
        Integer upperLeftY = 2;
        Integer lowerRightX = 100;
        Integer lowerRightY = 200;

        BoundingBoxCore boundingBoxCore = new BoundingBoxCore();
        boundingBoxCore.setUpperLeftX(upperLeftX);
        boundingBoxCore.setUpperLeftY(upperLeftY);
        boundingBoxCore.setLowerRightX(lowerRightX);
        boundingBoxCore.setLowerRightY(lowerRightY);

        mvc.perform(put("/api/verses/{pageId}", pageId).header("Authorization", TOKEN)
                        .content(objectMapper.writeValueAsString(boundingBoxCore)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    public void shouldDeleteVerse()
            throws Exception {
        Integer id = 2;

        mvc.perform(delete("/api/verses/{verseId}", id).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

    }


    @Test
    public void shouldNotMoveVerseUpWhenHasNoPermission()
            throws Exception {

        int verseId = 14;

        mvc.perform(put("/api/verses/{verseId}/move-up", verseId).header("Authorization", TOKEN))
                .andExpect(status().isForbidden());
    }


    @Test
    public void shouldMoveVerseDown()
            throws Exception {

        int verseId = 16;

        mvc.perform(put("/api/verses/{verseId}/move-down", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(14))).andExpect(jsonPath("$[0].id", is(17)))
                .andExpect(jsonPath("$[0].active", is(false))).andExpect(jsonPath("$[1].id", is(16)))
                .andExpect(jsonPath("$[1].active", is(true))).andExpect(jsonPath("$[2].id", is(18)))
                .andExpect(jsonPath("$[2].active", is(false)));
    }


    @Test
    public void shouldUpdateVerseCoordinates()
            throws Exception {
        int verseId = 1;

        Integer upperLeftX = 1;
        Integer upperLeftY = 2;
        Integer lowerRightX = 100;
        Integer lowerRightY = 200;

        BoundingBoxCore boundingBoxCore = new BoundingBoxCore();
        boundingBoxCore.setUpperLeftX(upperLeftX);
        boundingBoxCore.setUpperLeftY(upperLeftY);
        boundingBoxCore.setLowerRightX(lowerRightX);
        boundingBoxCore.setLowerRightY(lowerRightY);

        mvc.perform(put("/api/verses/{verseId}", verseId).header("Authorization", TOKEN)
                        .content(objectMapper.writeValueAsString(boundingBoxCore)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    public void shouldChangeVerseStatusWhenUserIsContributor()
            throws Exception {
        int verseId = 1;

        String token = LogInService.getJWTToken(mvc, "transcriber@wp.pl", PASSWORD);

        mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$", is(String.valueOf(VerseStatus.TRANSCRIBED))));

        mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$", is(String.valueOf(VerseStatus.NONE))));
    }


    @Test
    public void shouldChangeVerseStatusWhenUserIsVerifier()
            throws Exception {
        int verseId = 1;

        mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$", is(String.valueOf(VerseStatus.VERIFIED))));

        mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$", is(String.valueOf(VerseStatus.INCORRECT))));

        mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$", is(String.valueOf(VerseStatus.TRANSCRIBED))));

        mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$", is(String.valueOf(VerseStatus.VERIFIED))));
    }


    private Page getPageById(int transcriptionId, int pageId)
            throws Exception {
        return objectMapper.readValue(mvc.perform(
                        get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, pageId)
                                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse().getContentAsString(), Page.class);
    }

    private ResultActions getPageResultById(int transcriptionId, int pageId)
            throws Exception {
        return mvc.perform(
                get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, pageId)
                        .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON));
    }


    @Test
    public void shouldUpdateProgressWhenAddVerse()
            throws Exception {

        int transcriptionId = 1;
        int pageId = 1;

        Page page = getPageById(transcriptionId, pageId);
        assertThat(page.getPercentOfTranscribed(), is(16));

        Integer upperLeftX = 1;
        Integer upperLeftY = 2;
        Integer lowerRightX = 100;
        Integer lowerRightY = 200;

        BoundingBoxCore boundingBoxCore = new BoundingBoxCore();
        boundingBoxCore.setUpperLeftX(upperLeftX);
        boundingBoxCore.setUpperLeftY(upperLeftY);
        boundingBoxCore.setLowerRightX(lowerRightX);
        boundingBoxCore.setLowerRightY(lowerRightY);

        LayerCore layerCore = new LayerCore();
        layerCore.setTranscriptionId((long) transcriptionId);
        layerCore.setDescription("test");
        layerCore.setName("test");

        String jsonResponse = mvc.perform(post("/api/transcriptions/{transcriptionId}/layer", transcriptionId)

                        .header("Authorization", TOKEN)
                        .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(layerCore))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("name", is(layerCore.getName())))
                .andExpect(jsonPath("description", is(layerCore.getDescription())))
                .andReturn().getResponse().getContentAsString();

        Long layerId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, LayerDto.class).getId();

        mvc.perform(post("/api/verses/{pageId}", pageId).header("Authorization", TOKEN)
                        .param("layerId", layerId.toString())
                        .content(objectMapper.writeValueAsString(boundingBoxCore)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[0].status", is("none")));

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, pageId)
                        .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id", is(pageId)))
                .andExpect(jsonPath("percentOfTranscribed", is(14)));

    }


    @Test
    public void shouldUpdateProgressWhenDeleteTranscribedVerse()
            throws Exception {
        int transcriptionId = 1;
        int pageId = 1;

        for (int verseId = 1; verseId <= 5; verseId++) {
            mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", TOKEN))
                    .andExpect(status().isOk()).andExpect(jsonPath("$", is(String.valueOf(VerseStatus.VERIFIED))));

            mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", TOKEN))
                    .andExpect(status().isOk()).andExpect(jsonPath("$", is(String.valueOf(VerseStatus.INCORRECT))));

            mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", TOKEN))
                    .andExpect(status().isOk()).andExpect(jsonPath("$", is(String.valueOf(VerseStatus.TRANSCRIBED))));
        }

        Page updatedPage = getPageById(transcriptionId, pageId);
        assertThat(updatedPage.getPercentOfTranscribed(), is(100));

        int verseToBeDeletedId = 2;
        mvc.perform(delete("/api/verses/{verseId}", verseToBeDeletedId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        updatedPage = getPageById(transcriptionId, pageId);
        assertThat(updatedPage.getPercentOfTranscribed(), is(100));
    }


    @Test
    public void shouldUpdateProgressWhenDeleteVerifiedVerse()
            throws Exception {
        int transcriptionId = 1;
        int pageId = 1;

        for (int verseId = 1; verseId <= 5; verseId++) {
            mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", TOKEN))
                    .andExpect(status().isOk()).andExpect(jsonPath("$", is(String.valueOf(VerseStatus.VERIFIED))));
        }

        Page updatedPage = getPageById(transcriptionId, pageId);
        assertThat(updatedPage.getPercentOfTranscribed(), is(100));

        int verseToBeDeletedId = 2;
        mvc.perform(delete("/api/verses/{verseId}", verseToBeDeletedId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        updatedPage = getPageById(transcriptionId, pageId);
        assertThat(updatedPage.getPercentOfTranscribed(), is(100));
    }


    @Test
    public void shouldUpdateProgressWhenDeleteVerseWithoutStatus()
            throws Exception {
        int transcriptionId = 1;
        int pageId = 1;

        int verseToBeDeletedId = 2;
        mvc.perform(delete("/api/verses/{verseId}", verseToBeDeletedId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        Page page = getPageById(transcriptionId, pageId);
        assertThat(page.getPercentOfTranscribed(), is(20));
    }


    @Test
    public void shouldUpdateProgressWhenChangeVerseStatus()
            throws Exception {
        int transcriptionId = 1;
        int pageId = 1;

        Page page = getPageById(transcriptionId, pageId);
        assertThat(page.getPercentOfTranscribed(), is(16));

        int verseId = 1;

        mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$", is(String.valueOf(VerseStatus.VERIFIED))));

        Page updatedPage = getPageById(transcriptionId, pageId);
        assertThat(updatedPage.getPercentOfTranscribed(), is(33));

        mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$", is(String.valueOf(VerseStatus.INCORRECT))));

        updatedPage = getPageById(transcriptionId, pageId);
        assertThat(updatedPage.getPercentOfTranscribed(), is(16));

        mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$", is(String.valueOf(VerseStatus.TRANSCRIBED))));

        updatedPage = getPageById(transcriptionId, pageId);
        assertThat(updatedPage.getPercentOfTranscribed(), is(33));

        mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$", is(String.valueOf(VerseStatus.VERIFIED))));

        updatedPage = getPageById(transcriptionId, pageId);
        assertThat(updatedPage.getPercentOfTranscribed(), is(33));
    }


    @Test
    public void shouldChangeAllVersesOnPageLayerStatusWhenIsCalledByContributor()
            throws Exception {
        int transcriptionId = 1;
        int pageId = 1;

        getPageResultById(transcriptionId, pageId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("percentOfTranscribed", is(16)));

        String token = LogInService.getJWTToken(mvc, "transcriber@wp.pl", PASSWORD);

        mvc.perform(put("/api/verses/{pageId}/change-all-status?layerId=1&status=transcribed", pageId).header("Authorization", token))
                .andExpect(status().isOk());

        getPageResultById(transcriptionId, pageId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("percentOfTranscribed", is(50)));

        mvc.perform(put("/api/verses/{pageId}/change-all-status?layerId=2&status=transcribed", pageId).header("Authorization", token))
                .andExpect(status().isOk());

        getPageResultById(transcriptionId, pageId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("percentOfTranscribed", is(100)));
    }


    @Test
    public void shouldChangeAllVersesOnPageLayerStatusWhenIsCalledByContributorAndOneVerseStatusIsVerified()
            throws Exception {
        int transcriptionId = 1;
        int pageId = 1;

        mvc.perform(put("/api/verses/{verseId}/status", 1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("$", is(String.valueOf(VerseStatus.VERIFIED))));

        getPageResultById(transcriptionId, pageId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("percentOfTranscribed", is(16)));

        String token = LogInService.getJWTToken(mvc, "transcriber@wp.pl", PASSWORD);

        mvc.perform(put("/api/verses/{pageId}/change-all-status?layerId=1&status=transcribed", pageId).header("Authorization", token))
                .andExpect(status().isOk());

        getPageResultById(transcriptionId, pageId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("percentOfTranscribed", is(50)));
    }


    @Test
    public void shouldNotChangeAllVersesOnPageLayerStatusWhenThereIsNoGivenPage()
            throws Exception {
        int pageId = 999;

        mvc.perform(put("/api/verses/{pageId}/change-all-status?layerId=1&status=transcribed", pageId).header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }


    @Test
    public void shouldChangeAllVersesOnPageLayerStatusWhenIsCalledByVerifierOrAdmin()
            throws Exception {
        int transcriptionId = 1;
        int pageId = 1;

        getPageResultById(transcriptionId, pageId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("percentOfTranscribed", is(16)));

        mvc.perform(put("/api/verses/{pageId}/change-all-status?layerId=1&status=verified", pageId).header("Authorization", TOKEN))
                .andExpect(status().isOk());

        mvc.perform(put("/api/verses/{pageId}/change-all-status?layerId=2&status=verified", pageId).header("Authorization", TOKEN))
                .andExpect(status().isOk());

        getPageResultById(transcriptionId, pageId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("percentOfTranscribed", is(100)));
    }


    @Test
    public void shouldChangeNoVersesOnPageLayerStatusWhenPageHasNotVerses()
            throws Exception {
        int transcriptionId = 44;
        int pageId = 106;

        getPageResultById(transcriptionId, pageId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("percentOfTranscribed", is(0)));

        mvc.perform(put("/api/verses/{pageId}/change-all-status?layerId=1&status=transcribed", pageId)
                        .header("Authorization", LogInService.getJWTToken(mvc, "email2@wp.pl", "myPassword")))
                .andExpect(status().isOk());

        getPageResultById(transcriptionId, pageId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("percentOfTranscribed", is(0)));
    }


    @Test
    public void shouldNotChangeAllVersesOnPageLayerStatusWhenHasReadOnlyPermission()
            throws Exception {
        int transcriptionId = 2;
        int pageId = 6;
        String token = LogInService.getJWTToken(mvc, "email2@wp.pl", PASSWORD);

        getPageResultById(transcriptionId, pageId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("percentOfTranscribed", is(0)));

        mvc.perform(put("/api/verses/{pageId}/change-all-status?layerId=1&status=transcribed", pageId).header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldNotChangeAllVersesOnPageLayerStatusToVerifiedIfHaveOnlyTranscriberPermission()
            throws Exception {
        int pageId = 1;
        String token = LogInService.getJWTToken(mvc, "transcriber@wp.pl", PASSWORD);

        mvc.perform(put("/api/verses/{pageId}/change-all-status?layerId=1&status=verified", pageId).header("Authorization", token))
                .andExpect(status().isForbidden());
    }


    @Test
    public void shouldBeAbleToSaveEmptyVerse()
            throws Exception {

        long verseId = 1L;
        String tokenForContributor = LogInService.getJWTToken(mvc, "transcriber@wp.pl", PASSWORD);
        mvc.perform(put("/api/verses/{verseId}/modify-content", verseId).contentType("application/json")
                .header("Authorization", tokenForContributor)).andExpect(status().isOk());
    }


    @Test
    public void shouldThrowExceptionWhenVerseIsVerifiedAndUserIsNotAdminOrVerifier()
            throws Exception {
        long verseId = 72L;
        String tokenForContributor = LogInService.getJWTToken(mvc, "transcriber@wp.pl", PASSWORD);

        mvc.perform(put("/api/verses/{verseId}/status", verseId).header("Authorization", tokenForContributor))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("code", is("verses.error.alreadyVerified")));
        mvc.perform(
                        put("/api/verses/{verseId}/modify-content", verseId).contentType("application/json").content("content")
                                .header("Authorization", tokenForContributor)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("code", is("verses.error.alreadyVerified")));
        mvc.perform(put("/api/verses/{verseId}/formatting/clear", verseId).header("Authorization", tokenForContributor))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("code", is("verses.error.alreadyVerified")));
        mvc.perform(put("/api/verses/{verseId}/content-recognition", verseId).param("lang", "pol")
                        .header("Authorization", tokenForContributor)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("code", is("verses.error.alreadyVerified")));
        mvc.perform(put("/api/verses/{verseId}/move-up", verseId).header("Authorization", tokenForContributor))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("code", is("verses.error.alreadyVerified")));
        mvc.perform(put("/api/verses/{verseId}/move-down", verseId).header("Authorization", tokenForContributor))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("code", is("verses.error.alreadyVerified")));
        mvc.perform(delete("/api/verses/{verseId}", verseId).header("Authorization", tokenForContributor))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("code", is("verses.error.alreadyVerified")));
        mvc.perform(put("/api/verses/{verseId}/image", verseId).header("Authorization", tokenForContributor))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("code", is("verses.error.alreadyVerified")));
        mvc.perform(put("/api/verses/{verseId}", verseId).contentType("application/json").content(
                        "{\n" + "  \"upperLeftX\": 0,\n" + "  \"upperLeftY\": 0,\n" + "  \"lowerRightX\": 0,\n"
                                + "  \"lowerRightY\": 0\n" + "}").header("Authorization", tokenForContributor))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("code", is("verses.error.alreadyVerified")));
    }


    @Test
    public void shouldDeleteVersesWhenAllVersesIdOnList()
            throws Exception {
        Page page = getPageById(1, 1);
        assertEquals(6, page.getVerses().size());

        mvc.perform(delete("/api/verses").
                param("versesIds", "1").
                param("versesIds", "2").
                param("versesIds", "3").
                param("versesIds", "4").
                param("versesIds", "5").
                param("versesIds", "72").
                header("Authorization", TOKEN)).andExpect(status().isOk());

        Page pageAfterDeleting = getPageById(1, 1);
        assertEquals(0, pageAfterDeleting.getVerses().size());
    }

    @Test
    public void shouldDeleteVersesWhenOneVersesIdOnList()
            throws Exception {
        Page page = getPageById(1, 1);
        assertEquals(6, page.getVerses().size());

        mvc.perform(delete("/api/verses").
                param("versesIds", "1").
                header("Authorization", TOKEN)).andExpect(status().isOk());

        Page pageAfterDeleting = getPageById(1, 1);
        assertEquals(5, pageAfterDeleting.getVerses().size());
    }
}
