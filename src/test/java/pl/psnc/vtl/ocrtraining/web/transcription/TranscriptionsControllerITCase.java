package pl.psnc.vtl.ocrtraining.web.transcription;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.ocrtraining.domain.training.PendingOcrCorrection;
import pl.psnc.vtl.ocrtraining.domain.training.dto.OcrCorrection;
import pl.psnc.vtl.ocrtraining.domain.training.dto.OcrCorrectionsBatch;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TranscriptionsControllerITCase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;


    @Test
    void shouldHandleOcrCorrections() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        List<OcrCorrection> corrections = Arrays.asList(
                new OcrCorrection("http://localhost/webimages/corrections/1", "1-text", "pol"),
                new OcrCorrection("http://localhost/webimages/corrections/2", "2-text", "eng"),
                new OcrCorrection("http://localhost/webimages/corrections/3", "3-text", "lat"),
                new OcrCorrection("http://localhost/webimages/corrections/4", "4-text", "rus"),
                new OcrCorrection("http://localhost/webimages/corrections/5", "5-text", "deu")
        );

        OcrCorrectionsBatch correctionsBatch = new OcrCorrectionsBatch(corrections);

        mockMvc.perform(
                post("/api/training/ocr/corrections")
                        .content(objectMapper.writeValueAsString(correctionsBatch))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        assertPendingCorrectionsExist(corrections, entityManager);
    }

    @Test
    void shouldReturnErrorOnEmptyBatch() throws Exception {
        mockMvc.perform(
                post("/api/training/ocr/corrections")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    private void assertPendingCorrectionsExist(List<OcrCorrection> corrections, EntityManager entityManager) {
        CriteriaQuery<PendingOcrCorrection> query =
                entityManager.getCriteriaBuilder().createQuery(PendingOcrCorrection.class);
        Map<String, PendingOcrCorrection> pendingCorrections = entityManager.createQuery(
                query.select(query.from(PendingOcrCorrection.class))).getResultList()
                .stream()
                .collect(
                        Collectors.toMap(
                                PendingOcrCorrection::getIiifImageUrl,
                                Function.identity()));

        corrections.forEach(correction -> {
            PendingOcrCorrection pendingCorrection =
                    pendingCorrections.getOrDefault(correction.getIiifImageUrl(), null);

            assertNotNull(pendingCorrection);
            assertEquals(correction.getIiifImageUrl(), pendingCorrection.getIiifImageUrl());
            assertEquals(correction.getCorrectText(), pendingCorrection.getCorrectText());
            assertEquals(correction.getLang(), pendingCorrection.getLang());
        });
    }
}