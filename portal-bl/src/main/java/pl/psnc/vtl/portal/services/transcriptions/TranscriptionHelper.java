package pl.psnc.vtl.portal.services.transcriptions;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import pl.psnc.vtl.portal.model.transcriptions.Actor;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

import java.util.Iterator;

@UtilityClass
public class TranscriptionHelper {

    public String findTitleLabel(Transcription transcription) {
        if (transcription.getAnalyticInfo() != null && !StringUtils.isEmpty(transcription.getAnalyticInfo().
                getTitle())) {
            return transcription.getAnalyticInfo().getTitle();
        } else if (transcription.getMonographicInfo() != null && !StringUtils
                .isEmpty(transcription.getMonographicInfo().getTitle())) {
            return transcription.getMonographicInfo().getTitle();
        }
        return null;
    }

    public String findAuthorsLabel(Transcription transcription) {
        StringBuilder authors = new StringBuilder();
        if (transcription.getAnalyticInfo() != null && transcription.getAnalyticInfo().getActors() != null) {
            Iterator<Actor> it = transcription.getAnalyticInfo().getActors().iterator();
            while (it.hasNext()) {
                authors.append(it.next().getName());
                if (it.hasNext()) {
                    authors.append("; ");
                }
            }
        } else if (transcription.getMonographicInfo() != null
                && transcription.getMonographicInfo().getActors() != null) {
            Iterator<Actor> it = transcription.getMonographicInfo().getActors().iterator();
            while (it.hasNext()) {
                authors.append(it.next().getName());
                if (it.hasNext()) {
                    authors.append("; ");
                }
            }
        }
        return authors.toString();
    }

}
