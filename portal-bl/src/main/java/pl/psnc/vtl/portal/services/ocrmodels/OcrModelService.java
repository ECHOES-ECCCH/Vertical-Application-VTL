package pl.psnc.vtl.portal.services.ocrmodels;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.model.ocrmodels.OcrModel;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.ocrmodels.OcrModelRepository;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static java.lang.Long.parseLong;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class OcrModelService {

    @Value("${vtl.ocr-models-dir}")
    private Path modelsDir;
    @Value("${vtl.conf-dir}")
    private String filesLocation;

    private final OcrModelRepository ocrModelRepository;
    private final LoggedInUserHolder loggedInUserHolder;

    @Transactional(readOnly = true)
    public List<OcrModel> getOcrModels() {
        User loggedInUser = loggedInUserHolder.getLoggedInUser();
        return ocrModelRepository.findAllPublicOrOwnedBy(loggedInUser);
    }

    /**
     * @param modelIds list of model ids separated by +
     * @return engine and relative model paths separated by +
     */
    @Transactional(readOnly = true)
    public Pair<String, String> parseModelParam(String modelIds) {
        StringBuilder sb = new StringBuilder();
        String engine = null;
        for (String modelId : modelIds.split("[+]")) {
            OcrModel ocrModel = ocrModelRepository.findById(parseLong(modelId)).orElse(null);
            if (ocrModel == null)
                throw new IllegalArgumentException("No such model: " + modelId);
            if (engine == null) {
                engine = ocrModel.getEngine().toString();
            } else if (!engine.equals(ocrModel.getEngine().toString())) {
                throw new IllegalArgumentException("Models must be of the same engine");
            }
            String modelPath = getModelRelativePath(ocrModel).toString();
            sb.append(modelPath).append("+");
        }
        return Pair.of(engine, sb.substring(0, sb.length() - 1));
    }

    @Transactional(readOnly = true)
    public List<OcrModel> getOcrModels(String modelIds) {
        List<Long> ids = Arrays.stream(modelIds.split("[+]")).map(Long::parseLong).collect(toList());
        List<OcrModel> models = ocrModelRepository.findAllById(ids);
        // FIXME check access
        models.sort(comparing(m -> ids.indexOf(m.getId())));
        return models;
    }

    @Transactional(readOnly = true)
    public List<OcrModel> getOcrModelsForTranscription(long transcriptionId) {
        // FIXME include models shared with transcription
        return ocrModelRepository.findAllPublic();
    }

    public Path getModelAbsolutePath(OcrModel ocrModel) {
        return modelsDir.resolve(getModelRelativePath(ocrModel));
    }

    public Path getModelRelativePath(OcrModel ocrModel) {
        return Path.of("m" + ocrModel.getId(), "model");
    }
}
