package pl.psnc.vtl.portal.repositories.oaipmh;

import lombok.Builder;
import lombok.Data;
import org.dspace.xoai.dataprovider.model.ItemIdentifier;
import org.dspace.xoai.dataprovider.model.Set;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class OaiItemIdentifier implements ItemIdentifier {

    private String identifier;
    private Date datestamp;
    private List<Set> sets;
    private boolean deleted;

    public static OaiItemIdentifier fromTranscription(Transcription transcription) {
        return OaiItemIdentifier.builder()
                .datestamp(Date.from(transcription.getLastModificationDate().toInstant(ZoneOffset.UTC)))
                .identifier(OaiItemUtils.buildOaiId(transcription.getId()))
                .deleted(false)
                .sets(List.of())
                .build();
    }
}
