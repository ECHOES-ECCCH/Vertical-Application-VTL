package pl.psnc.vtl.portal.services.layers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.conf.logging.LogAction;
import pl.psnc.vtl.portal.dto.layers.LayerCore;
import pl.psnc.vtl.portal.dto.layers.LayerDto;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.repositories.layers.LayerRepository;
import pl.psnc.vtl.portal.repositories.pages.PageLayersRepository;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionExportService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LayerService {

    private final LayerRepository layerRepository;

    private final LayerValidator layerValidator;

    private final PageLayerService pageLayerService;

    private final PageLayersRepository pageLayersRepository;

    private final TranscriptionExportService transcriptionExportService;

    @Value("${vtl.pagination.layers.minimal-layer}")
    private Integer minimalLayerNumber;

    @Value("${vtl.pagination.layers.maximal-layer}")
    private Integer maximalLayerNumber;

    public Layer createLayer(LayerCore layerCore) {

        if (!checkLayerNumber(layerCore.getTranscriptionId())) {
            throw new IllegalArgumentException(String.format(
                    "The layers limit was exceeded, it must be within range of %s and %s. Therefore make changes before creating another layer.",
                    minimalLayerNumber, maximalLayerNumber));
        }

        Layer layer = layerValidator.create(layerCore, null);
        layer = layerRepository.save(layer);

        transcriptionExportService.resetTranscriptionExportStatus(layerCore.getTranscriptionId());

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Created Layer with id - {} for Transcription with id - {}.", layer.getId(), layerCore.getTranscriptionId());
        LogAction.clearMDCAction();

        return layer;
    }


    public Layer getLayer(long layerId) {
        return layerRepository.findById(layerId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Layer.class.getName() + " with id " + layerId));
    }


    public List<Layer> getAllLayersPerTranscription(long transcriptionId) {
        return layerRepository.findAllByTranscriptionIdOrderById(transcriptionId);
    }

    /**
     * Retrieves first (with the lowest identifier) layer for given transcription.
     * @param transcriptionId
     * @return
     */
    public Layer getFirstLayerForTranscription(long transcriptionId) {
        return layerRepository.findFirstByTranscriptionIdOrderById(transcriptionId);
    }

    public List<PageLayer> getAllPageLayersPerLayer(long layerId) {
        return pageLayersRepository.findAllByLayerId(layerId);
    }

    public void deleteLayer(long id) {
        if (!layerRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Layer.class.getName() + " with id " + id);
        }
        pageLayerService.deleteByLayerId(id);

        Layer layer = getLayer(id);
        layerRepository.delete(layer);

        transcriptionExportService.resetTranscriptionExportStatus(layer.getTranscription().getId());

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Removed Layer with id - {}.", id);
        LogAction.clearMDCAction();
    }

    public void deleteLayers(long transcriptionId) {
        List<Layer> layers = layerRepository.findAllByTranscriptionIdOrderById(transcriptionId);
        layers.forEach(layer -> {
            pageLayerService.deleteByLayerId(layer.getId());
            layerRepository.delete(layer);

            LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
            log.info("Removed Layer with id - {}.", layer.getId());
            LogAction.clearMDCAction();
        });
    }

    public LayerDto updateLayer(long layerId, LayerCore layerCore) {
        Layer layer = layerValidator.create(layerCore, layerId);
        layer = layerRepository.save(layer);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Updated Layer with id - {} from Transcription with id - {}.", layerId, layerCore.getTranscriptionId());
        LogAction.clearMDCAction();

        return LayerMapperImpl.INSTANCE.toDto(layer);
    }


    private boolean checkLayerNumber(long transcriptionId) {
        int layerNumber = layerRepository.countLayersByTranscriptionId(transcriptionId);
        return 0 <= layerNumber && layerNumber < maximalLayerNumber;
    }

    public void createPageLayers(List<Long> pagesId, Layer layer) {
        pageLayerService.createPageLayers(pagesId, layer);
    }
}
