package pl.psnc.vtl.portal.controllers.indexes;

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
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.conf.TestJsonMapper;
import pl.psnc.vtl.portal.dto.indexes.IndexCore;
import pl.psnc.vtl.portal.model.indexes.Index;
import pl.psnc.vtl.portal.model.indexes.IndexType;

import javax.transaction.Transactional;

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
public class IndexControllerITCase {

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
    public void shouldCreatePersonIndex()
            throws Exception {

        IndexCore indexCore = new IndexCore();
        indexCore.setIndexType(IndexType.PERSON);
        indexCore.setName("Test");
        indexCore.setAccessibleOn("https://www.example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue())).andExpect(jsonPath("type", is(IndexType.PERSON.getValue())))
                .andExpect(jsonPath("name", is(indexCore.getName())))
                .andExpect(jsonPath("accessibleOn", nullValue()));

    }


    @Test
    public void shouldCreatePlaceIndex()
            throws Exception {

        IndexCore indexCore = new IndexCore();
        indexCore.setIndexType(IndexType.PLACE);
        indexCore.setName("Test");
        indexCore.setAccessibleOn("https://www.example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue())).andExpect(jsonPath("type", is(IndexType.PLACE.getValue())))
                .andExpect(jsonPath("name", is(indexCore.getName())))
                .andExpect(jsonPath("accessibleOn", is(indexCore.getAccessibleOn())));

    }


    @Test
    public void shouldCreateTermIndex()
            throws Exception {

        IndexCore indexCore = new IndexCore();
        indexCore.setIndexType(IndexType.TERM);
        indexCore.setName("Test");
        indexCore.setAccessibleOn("https://www.example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("type", is(IndexType.TERM.getValue())))
                .andExpect(jsonPath("name", is(indexCore.getName())))
                .andExpect(jsonPath("accessibleOn", nullValue()));

    }

    @Test
    public void shouldCreateIndexAndAddItToVerses() throws Exception {
        IndexCore indexCore = new IndexCore();
        indexCore.setIndexType(IndexType.TERM);
        indexCore.setName("Test");
        indexCore.setAccessibleOn("https://www.example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        mvc.perform(put("/api/indexes/{indexId}/{verseId}", 1L, 1L)
                        .header("Authorization", TOKEN))
                .andExpect(jsonPath("verseIds", hasSize(1)))
                .andExpect(jsonPath("verseIds[0]", is(1)));

        mvc.perform(put("/api/indexes/{indexId}/{verseId}", 1L, 2L)
                        .header("Authorization", TOKEN))
                .andExpect(jsonPath("verseIds", hasSize(2)))
                .andExpect(jsonPath("verseIds[0]", is(1)))
                .andExpect(jsonPath("verseIds[1]", is(2)));

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", 1L, 1L).header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("verses[0].indexes", hasSize(1)))
                .andExpect(jsonPath("verses[2].indexes", hasSize(1)));
        //verses[0] has id 1, verses[2] has id 2
    }

    @Test
    public void shouldRemoveIndexesFromVersesAndRemoveIndexIfNotUsed() throws Exception {
        IndexCore indexCore = new IndexCore();
        indexCore.setIndexType(IndexType.TERM);
        indexCore.setName("Test");
        indexCore.setAccessibleOn("https://www.example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        mvc.perform(put("/api/indexes/{indexId}/{verseId}", 1L, 1L)
                .header("Authorization", TOKEN));

        mvc.perform(put("/api/indexes/{indexId}/{verseId}", 1L, 2L)
                .header("Authorization", TOKEN));

        mvc.perform(delete("/api/indexes/{indexId}/{verseId}", 1L, 1L)
                .header("Authorization", TOKEN));

        mvc.perform(delete("/api/indexes/{indexId}/{verseId}", 1L, 2L)
                .header("Authorization", TOKEN));

        mvc.perform(get("/api/indexes/{indexId}", 1L)
                        .header("Authorization", TOKEN))
                .andExpect(status().is5xxServerError());
    }


    @Test
    public void shouldUpdateIndex()
            throws Exception {

        IndexCore indexCore = new IndexCore();
        indexCore.setIndexType(IndexType.PLACE);
        indexCore.setName("Test");
        indexCore.setAccessibleOn("https://www.example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);
        log.debug("JSON: " + payload);

        String indexResponse = mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("type", is(IndexType.PLACE.getValue())))
                .andExpect(jsonPath("name", is("Test")))
                .andExpect(jsonPath("accessibleOn", is(indexCore.getAccessibleOn()))).andReturn().getResponse()
                .getContentAsString();

        Index index = mapper.readValue(indexResponse, Index.class);
        long newIndexId = index.getId();

        indexCore.setIndexType(IndexType.PLACE);
        indexCore.setName("Test 2");
        indexCore.setAccessibleOn("https://www.example2.com");

        String payloadUpdate = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);
        log.debug("JSON: " + payloadUpdate);

        mvc.perform(put("/api/indexes/{indexId}", newIndexId).header("Authorization", TOKEN).content(payloadUpdate)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("type", is(IndexType.PLACE.getValue())))
                .andExpect(jsonPath("name", is("Test 2")))
                .andExpect(jsonPath("accessibleOn", is("https://www.example2.com")));

    }


    @Test
    public void shouldRetrieveAllIndexes()
            throws Exception {

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

        mvc.perform(get("/api/indexes/").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test")));
    }


    @Test
    public void shouldRetrieveIndex()
            throws Exception {

        IndexCore indexCore = new IndexCore();
        indexCore.setIndexType(IndexType.PERSON);
        indexCore.setName("Test");
        indexCore.setAccessibleOn("https://www.example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);
        log.debug("JSON: " + payload);

        String indexResponse = mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("type", is(IndexType.PERSON.getValue())))
                .andExpect(jsonPath("name", is("Test")))
                .andExpect(jsonPath("accessibleOn", nullValue())).andReturn().getResponse().getContentAsString();

        Index index = mapper.readValue(indexResponse, Index.class);
        long newIndexId = index.getId();

        mvc.perform(get("/api/indexes/{indexId}", newIndexId).header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("name", is("Test")));

    }


    @Test
    public void shouldDeleteIndex()
            throws Exception {

        IndexCore indexCore = new IndexCore();
        indexCore.setIndexType(IndexType.PERSON);
        indexCore.setName("Test");
        indexCore.setAccessibleOn("https://www.example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);
        log.debug("JSON: " + payload);

        String indexResponse = mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("type", is(IndexType.PERSON.getValue())))
                .andExpect(jsonPath("name", is("Test")))
                .andExpect(jsonPath("accessibleOn", nullValue()))
                .andReturn().getResponse().getContentAsString();

        Index index = mapper.readValue(indexResponse, Index.class);
        long newIndexId = index.getId();

        mvc.perform(delete("/api/indexes/{indexId}", newIndexId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }


    @Test
    public void shouldSearchPhrase()
            throws Exception {
        IndexCore indexCore = new IndexCore();
        indexCore.setIndexType(IndexType.TERM);
        indexCore.setName("Test");
        indexCore.setAccessibleOn("https://www.example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("type", is(IndexType.TERM.getValue())))
                .andExpect(jsonPath("name", is(indexCore.getName())))
                .andExpect(jsonPath("accessibleOn", nullValue()));

        IndexCore indexCore2 = new IndexCore();
        indexCore2.setIndexType(IndexType.TERM);
        indexCore2.setName("Xyz");
        indexCore2.setAccessibleOn("https://www.example.com");

        String payload2 = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore2);
        log.debug("JSON: " + payload2);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload2)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue())).andExpect(jsonPath("type", is(IndexType.TERM.getValue())))
                .andExpect(jsonPath("name", is(indexCore2.getName())))
                .andExpect(jsonPath("accessibleOn", nullValue()));

        mvc.perform(get("/api/indexes/search").header("Authorization", TOKEN).param("searchPhrase", "Test")
                        .param("type", "").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].type", is(IndexType.TERM.getValue())))
                .andExpect(jsonPath("$[0].name", is(indexCore.getName())))
                .andExpect(jsonPath("$[0].accessibleOn", nullValue()));
    }


    @Test
    public void shouldSearchPhraseAndType()
            throws Exception {
        IndexCore indexCore = new IndexCore();
        indexCore.setIndexType(IndexType.PERSON);
        indexCore.setName("Test");
        indexCore.setAccessibleOn("https://www.example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue())).andExpect(jsonPath("type", is(IndexType.PERSON.getValue())))
                .andExpect(jsonPath("name", is(indexCore.getName())))
                .andExpect(jsonPath("accessibleOn", nullValue()));

        IndexCore indexCore2 = new IndexCore();
        indexCore2.setIndexType(IndexType.PLACE);
        indexCore2.setName("Test");
        indexCore2.setAccessibleOn("https://www.example.com");

        String payload2 = TestJsonMapper.serializingObjectMapper().writeValueAsString(indexCore2);
        log.debug("JSON: " + payload2);

        mvc.perform(post("/api/indexes/").header("Authorization", TOKEN).content(payload2)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue())).andExpect(jsonPath("type", is(IndexType.PLACE.getValue())))
                .andExpect(jsonPath("name", is(indexCore2.getName())))
                .andExpect(jsonPath("accessibleOn", notNullValue()));

        mvc.perform(get("/api/indexes/search").header("Authorization", TOKEN).param("searchPhrase", "Test")
                        .param("type", "PERSON").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].type", is(IndexType.PERSON.getValue())))
                .andExpect(jsonPath("$[0].name", is(indexCore.getName())))
                .andExpect(jsonPath("$[0].accessibleOn", nullValue()));
    }
}
