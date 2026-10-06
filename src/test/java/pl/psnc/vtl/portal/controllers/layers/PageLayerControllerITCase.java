package pl.psnc.vtl.portal.controllers.layers;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.ResourceFileContentLoader;
import pl.psnc.vtl.portal.conf.TestJsonMapper;
import pl.psnc.vtl.portal.dto.indexes.IndexCore;
import pl.psnc.vtl.portal.dto.layers.LayerCore;
import pl.psnc.vtl.portal.dto.layers.LayerDto;
import pl.psnc.vtl.portal.dto.transcriptions.*;
import pl.psnc.vtl.portal.model.indexes.Index;
import pl.psnc.vtl.portal.model.indexes.IndexType;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

import javax.transaction.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

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
public class PageLayerControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String TOKEN;

    @Autowired
    private ObjectMapper mapper;

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";


    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
        mapper.registerModule(new JavaTimeModule());
    }


    @Test
    public void shouldGetPageLayersByLayerId() throws Exception {

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
    public void shouldNotGetPageLayerUnauthorized() throws Exception{
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
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldGetPageLayerPerLayerAndPage() throws Exception {
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

        Long pageId = 1L;
        Long layerId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, LayerDto.class).getId();

        mvc.perform(get("/api/transcriptions/{transcriptionId}/layer", transcriptionId)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is( layerCore.getName())))
                .andExpect(jsonPath("$[0].description", is( layerCore.getDescription())));

        mvc.perform(get("/api/pageLayers/layer/{layerId}/page/{pageId}", layerId ,pageId)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("layerId", is(layerId.intValue())))
                .andExpect(jsonPath("pageId", is(pageId.intValue())))
                .andExpect(jsonPath("verses", hasSize(0)));

    }

    @Test
    public void shouldGetPageLayersPerPage() throws Exception {
        Long transcriptionId = 1L;
        Long pageId = 1L;

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
                .andExpect(jsonPath("description", is( layerCore.getDescription())))
                .andReturn();


        mvc.perform(get("/api/transcriptions/{transcriptionId}/layer", transcriptionId)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is( layerCore.getName())))
                .andExpect(jsonPath("$[0].description", is( layerCore.getDescription())));

        mvc.perform(get("/api/pageLayers/page/{pageId}", pageId )
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].layerId", notNullValue()))
                .andExpect(jsonPath("$[0].pageId", is(pageId.intValue())))
                .andExpect(jsonPath("$[0].name", is(layerCore.getName())));

    }


    @Test
    public void shouldAddVerseToPageLayers() throws Exception {
        Long pageId = 1L;
        Long verseId = 1L;
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


        mvc.perform(put("/api/pageLayers/layer/{layerId}/page/{pageId}/verse/{verseId}", layerId, pageId, verseId )
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("layerId", notNullValue()))
                .andExpect(jsonPath("pageId", is(pageId.intValue())))
                .andExpect(jsonPath("name", is(layerCore.getName())))
                .andExpect(jsonPath("verses", hasSize(1)));
    }

    @Test
    public void shouldNotAddVerseUnauthorized() throws Exception {

        Long pageId = 1L;
        Long verseId = 1L;
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

        mvc.perform(put("/api/pageLayers/layer/{layerId}/page/{pageId}/verse/{verseId}", layerId, pageId, verseId )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

    }

    @Test
    public void shouldRemoveVerseFromPageLayer() throws Exception {
        Long pageId = 1L;
        Long verseId = 1L;
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

        mvc.perform(put("/api/pageLayers/layer/{layerId}/page/{pageId}/verse/{verseId}", layerId, pageId, verseId )
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("layerId", notNullValue()))
                .andExpect(jsonPath("pageId", is(pageId.intValue())))
                .andExpect(jsonPath("name", is(layerCore.getName())))
                .andExpect(jsonPath("verses", hasSize(1)));

        mvc.perform(delete("/api/pageLayers/layer/{layerId}/page/{pageId}/verse/{verseId}", layerId, pageId, verseId )
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("layerId", notNullValue()))
                .andExpect(jsonPath("pageId", is(pageId.intValue())))
                .andExpect(jsonPath("name", is(layerCore.getName())))
                .andExpect(jsonPath("verses", hasSize(0)));

        mvc.perform(get("/api/pageLayers/layer/{layerId}/page/{pageId}", layerId ,pageId)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("layerId", is(layerId.intValue())))
                .andExpect(jsonPath("pageId", is(pageId.intValue())))
                .andExpect(jsonPath("verses", hasSize(0)));
    }

    @Test
    public void shouldRemoveAllVersesFromPageLayer() throws Exception {

        Long pageId = 1L;
        Long verseId = 1L;
        Long secondVerseId = 2L;
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


        mvc.perform(put("/api/pageLayers/layer/{layerId}/page/{pageId}/verse/{verseId}", layerId, pageId, verseId )
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("layerId", notNullValue()))
                .andExpect(jsonPath("pageId", is(pageId.intValue())))
                .andExpect(jsonPath("name", is(layerCore.getName())))
                .andExpect(jsonPath("verses", hasSize(1)));

        mvc.perform(put("/api/pageLayers/layer/{layerId}/page/{pageId}/verse/{verseId}", layerId, pageId, secondVerseId )
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("layerId", notNullValue()))
                .andExpect(jsonPath("pageId", is(pageId.intValue())))
                .andExpect(jsonPath("name", is(layerCore.getName())))
                .andExpect(jsonPath("verses", hasSize(2)));

        mvc.perform(get("/api/pageLayers/layer/{layerId}/page/{pageId}", layerId ,pageId)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("layerId", is(layerId.intValue())))
                .andExpect(jsonPath("pageId", is(pageId.intValue())))
                .andExpect(jsonPath("verses", hasSize(2)));

        mvc.perform(delete("/api/pageLayers/layer/{layerId}/page/{pageId}", layerId, pageId)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("layerId", notNullValue()))
                .andExpect(jsonPath("pageId", is(pageId.intValue())))
                .andExpect(jsonPath("name", is(layerCore.getName())))
                .andExpect(jsonPath("verses", hasSize(0)));

    }


    @Test
    public void shouldChangeProgress() throws Exception {

        Long pageId = 1L;
        Long verseId = 1L;
        Long secondVerseId = 2L;
        Long transcriptionId = 1L;

        LayerCore layerCore = new LayerCore();
        layerCore.setTranscriptionId(transcriptionId);
        layerCore.setDescription("test");
        layerCore.setName("test");

        String jsonResponse = mvc.perform(post("/api/transcriptions/{transcriptionId}/layer", transcriptionId)
                        .header("Authorization", TOKEN)
                        .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(layerCore))
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue())).andExpect(jsonPath("name", is(layerCore.getName())))
                .andExpect(jsonPath("description", is(layerCore.getDescription()))).andReturn().getResponse().getContentAsString();

        Long layerId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, LayerDto.class).getId();

        mvc.perform(get("/api/transcriptions/{transcriptionId}/layer", transcriptionId).header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].name", is(layerCore.getName())))
                .andExpect(jsonPath("$[0].description", is(layerCore.getDescription())));

        mvc.perform(put("/api/pageLayers/layer/{layerId}/page/{pageId}/verse/{verseId}", layerId, pageId, verseId).header(
                        "Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("layerId", notNullValue()))
                .andExpect(jsonPath("pageId", is(pageId.intValue())))
                .andExpect(jsonPath("name", is(layerCore.getName())))
                .andExpect(jsonPath("verses", hasSize(1)));

        mvc.perform(put("/api/pageLayers/layer/{layerId}/page/{pageId}/verse/{verseId}", layerId, pageId, secondVerseId )
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("layerId", notNullValue()))
                .andExpect(jsonPath("pageId", is(pageId.intValue())))
                .andExpect(jsonPath("name", is(layerCore.getName())))
                .andExpect(jsonPath("verses", hasSize(2)));

        mvc.perform(put("/api/verses/{verseId}/status", verseId )
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/api/pageLayers/layer/{layerId}/page/{pageId}", layerId, pageId)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("layerId", notNullValue()))
                .andExpect(jsonPath("pageId", is(pageId.intValue())))
                .andExpect(jsonPath("name", is(layerCore.getName())))
                .andExpect(jsonPath("verses", hasSize(2)))
                .andExpect(jsonPath("percentOfTranscribed", is(50)))
                .andExpect(jsonPath("percentOfVerified", is(50)));

    }

    @Test
    public void shouldCreatePageLayersAfterAddingPagesFromPdf() throws Exception {

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

        newTranscription.setAnalyticInfo(analyticInfo);
        newTranscription.setMonographicInfo(monographicInfo);
        newTranscription.setProfileDesc(profileDesc);

        newTranscription.setAcceptedRegulations(true);

        String jsonTranscriptionResponse = mvc.perform(post("/api/transcriptions", newTranscription).header("Authorization", TOKEN)
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

                .andExpect(jsonPath("hidden", is(true)))
                .andReturn().getResponse().getContentAsString();

        Transcription transcription =
                TestJsonMapper.serializingObjectMapper().readValue(jsonTranscriptionResponse, Transcription.class);

        Long transcriptionId = transcription.getId();

        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("test1Pdf.pdf");

        MockMultipartFile file1 = new MockMultipartFile("file", "filename11c.pdf", "text/plain", ba);

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

      String jsonLayer = mvc.perform(get("/api/transcriptions/{transcriptionId}/layer", transcriptionId)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is( "First layer")))
                .andExpect(jsonPath("$[0].description", is( "First layer")))
              .andReturn().getResponse().getContentAsString();

        List<LayerDto> layerDtos =
                TestJsonMapper.serializingObjectMapper().readValue(jsonLayer, List.class);


        mvc.perform(get("/api/pageLayers/layer/{layerId}", 1L )
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

    }



}
