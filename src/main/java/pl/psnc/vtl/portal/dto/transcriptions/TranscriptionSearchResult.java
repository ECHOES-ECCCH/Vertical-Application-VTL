package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.dto.pages.PageWithHitCount;
import pl.psnc.vtl.portal.model.transcriptions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transcription search result object.
 */
@Data
@NoArgsConstructor
public class TranscriptionSearchResult {

    /**
     * Transcription reference.
     */
    private Transcription transcription;

    /**
     * List of pages with search hit count.
     */
    private List<PageWithHitCount> pagesWithHitCounts = new ArrayList<>();

    /**
     * Map of metadata types with hit search count.
     */
    private Map<MetadataType, Boolean> metadataHits = new HashMap<>();

    /**
     * Total search hit count.
     */
    private Long hitCount;

    /**
     * Does contain metadata hits.
     */
    private boolean hasMetadataHits;

    /**
     * Does contain content hits.
     */
    private boolean hasContentHits;

    private enum MetadataType {
        DESCRIPTION,
        DOCUMENT_TYPES,
        COLLECTION_NAME,
        CONTACT_NAME,
        GUIDELINES,
        TITLE,
        SUMMARY,
        KEYWORDS
    }

    public TranscriptionSearchResult(Transcription transcription) {
        this.transcription = transcription;
        this.hitCount = 1L;
    }

    public TranscriptionSearchResult(TranscriptionWithHitCount transcription) {
        this.transcription = transcription.getTranscription();
        this.hitCount = transcription.getHitCount();
    }

    public void addPage(PageWithHitCount page) {
        hasContentHits = true;
        pagesWithHitCounts.add(page);
    }

    public void checkMetadata(String q) {
        TranscriptionInfo info = transcription.getTranscriptionInfo();
        if(info != null) {
            metadataHits.put(MetadataType.DESCRIPTION, info.getDescription() != null && info.getDescription().contains(q));
            metadataHits.put(MetadataType.DOCUMENT_TYPES, info.getDocumentTypes() != null && info.getDocumentTypes().contains(q));
            metadataHits.put(MetadataType.COLLECTION_NAME, info.getCollectionName() != null && info.getCollectionName().contains(q));
            metadataHits.put(MetadataType.CONTACT_NAME, info.getContactName() != null && info.getContactName().contains(q));
            metadataHits.put(MetadataType.GUIDELINES, info.getGuidelines() != null && info.getGuidelines().contains(q));
        }

        AnalyticInfo info2 = transcription.getAnalyticInfo();
        if(info2 != null) {
            metadataHits.put(MetadataType.TITLE, info2.getTitle() != null && info2.getTitle().contains(q));
        }

        MonographicInfo info3 = transcription.getMonographicInfo();
        if(info3 != null) {
            metadataHits.put(MetadataType.TITLE, info3.getTitle() != null && info3.getTitle().contains(q));
        }

        ProfileDesc info4 = transcription.getProfileDesc();
        if(info4 != null) {
            metadataHits.put(MetadataType.SUMMARY, info4.getSummary() != null && info4.getSummary().contains(q));

            boolean keywordsHit = false;
            for(String keyword : info4.getKeywords()) {
                if(keyword.equals(q)) {
                    keywordsHit = true;
                    break;
                }
            }
            metadataHits.put(MetadataType.KEYWORDS, keywordsHit);
        }

        for(boolean metadata : metadataHits.values()) {
            if(metadata) {
                hasMetadataHits = true;
                break;
            }
        }
    }
}
