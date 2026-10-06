package pl.psnc.vtl.portal.repositories.oaipmh;

import org.dspace.xoai.dataprovider.model.Item;
import org.dspace.xoai.dataprovider.model.Set;
import org.dspace.xoai.model.oaipmh.About;
import org.dspace.xoai.model.oaipmh.Metadata;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class OaiItem implements Item {

    private final Transcription transcription;

    public OaiItem(Transcription transcription) {
        this.transcription = transcription;
    }

    @Override
    public List<About> getAbout() {
        return Collections.emptyList();
    }

    @Override
    public Metadata getMetadata() {
        return new Metadata(OaiItemUtils.convertToOaiDc(transcription));
    }

    @Override
    public String getIdentifier() {
        return OaiItemUtils.buildOaiId(transcription.getId());
    }

    @Override
    public Date getDatestamp() {
        return Date.from(transcription.getLastModificationDate().toInstant(ZoneOffset.UTC));
    }

    @Override
    public List<Set> getSets() {
        return Collections.emptyList();
    }

    @Override
    public boolean isDeleted() {
        return false;
    }
}
