package pl.psnc.vtl.portal.services.layers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import pl.psnc.vtl.portal.dto.layers.LayerCore;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.layers.LayerRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionRepository;
import pl.psnc.vtl.portal.services.ValidationException;

import javax.transaction.Transactional;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class LayerValidator {

    private final LayerRepository layerRepository;

    private final TranscriptionRepository transcriptionRepository;

    public Layer create(LayerCore layerCore, Long layerId) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(layerCore, "layer");
        Layer layer = getOrCreateLayer(layerId);

        if (StringUtils.isBlank(layerCore.getName())) {
            errors.rejectValue("name", "field.required", "Layer name is required.");
        } else {
            layer.setName(layerCore.getName());
        }

        layer.setDescription(layerCore.getDescription());
        if (layerId == null)
            layer.setType(layerCore.getType());

        if (!Objects.isNull(layerCore.getTranscriptionId())) {
            Transcription t = transcriptionRepository.findById(layerCore.getTranscriptionId()).orElse(null);
            if (t == null)
                errors.rejectValue("transcriptionId", "field.required", "Transcription Id don't exist, but this field is required to assign an layer to the appropriate transcription.");
            layer.setTranscription(t);
        } else {
            errors.rejectValue("transcriptionId", "field.required", "Transcription Id is required to assign an layer to the appropriate transcription.");
        }


        if (errors.hasErrors())
            throw new ValidationException(errors);

        return layer;
    }

    private Layer getOrCreateLayer(Long layerId) {
        if (layerId != null) {
            return layerRepository.getOne(layerId);
        } else return new Layer();
    }

}
