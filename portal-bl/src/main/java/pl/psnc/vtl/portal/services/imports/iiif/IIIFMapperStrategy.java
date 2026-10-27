package pl.psnc.vtl.portal.services.imports.iiif;

import org.apache.commons.lang3.StringUtils;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.MapperStrategy;
import pl.psnc.vtl.portal.services.imports.TextFormatValidator;
import pl.psnc.vtl.portal.utils.IIIFUtils;

public class IIIFMapperStrategy implements MapperStrategy {

    @Override
    public TranscriptionCore mapToImportTemplate(String content) {
        TranscriptionCore core = new TranscriptionCore();

        String label = IIIFUtils.getLabelFromManifestJson(content);
        if (label != null) {
            core.getAnalyticInfo().setTitle(label);
        } else {
            throw TextFormatValidator.getWrongTypeXMLException();
        }

        String externalId = IIIFUtils.getIdFromManifestJson(content);
        if (externalId != null) {
            core.setExternalId(externalId);
            core.setExternalUrl(externalId);
        } else {
            throw TextFormatValidator.getWrongTypeXMLException();
        }

        String summary = IIIFUtils.getSummaryFromManifestJson(content);
        if (summary != null) {
            core.getProfileDesc().setSummary(summary);
            core.getTranscriptionInfo().setDescription(summary);
        }

        String signature = IIIFUtils.getSignatureFromManifestJson(content);
        if (!StringUtils.isBlank(signature)) {
            core.getMonographicInfo().setSignature(signature);
        }

        return core;
    }
}
