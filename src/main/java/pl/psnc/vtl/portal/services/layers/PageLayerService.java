package pl.psnc.vtl.portal.services.layers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.layers.PageLayerCore;
import pl.psnc.vtl.portal.dto.layers.PageLayerDto;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.repositories.pages.PageLayersRepository;
import pl.psnc.vtl.portal.services.imports.BeanLoader;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PageLayerService {

    private final PageLayersValidator pageLayersValidator;

    private final PageLayersRepository pageLayersRepository;

    private final TranscriptionPermissionService transcriptionPermissionService;

    public List<PageLayerDto> getAllPageLayersPerLayer(Long layerId) {
        return PageLayerMapper.INSTANCE.toDto(pageLayersRepository.findAllByLayerId(layerId));
    }


    public List<PageLayerDto> getAllPageLayersPerPage(Long pageId) {
        List<PageLayer> result = pageLayersRepository.findAllByPageId(pageId);
        result.forEach(pageLayer -> transcriptionPermissionService.checkReadAccess(
                pageLayer.getPage().getTranscription().getId()));
        return PageLayerMapper.INSTANCE.toDto(result);
    }

    public PageLayerDto returnPageLayer(Long layerId, Long pageId) {
        PageLayer pageLayer = getPageLayer(pageId, layerId);
        transcriptionPermissionService.checkTranscribeAccess(pageLayer.getPage().getTranscription().getId());
        return PageLayerMapper.INSTANCE.toDto(pageLayer);
    }

    public void deleteByLayerId(Long layerId) {
        pageLayersRepository.deleteByLayerId(layerId);
    }

    public PageLayer getPageLayer(Long pageId, Long layerId) {
        if (layerId != null && pageId != null) {
            return pageLayersRepository.findByPageIdAndLayerId(pageId, layerId).get();
        } else {
            throw new EntityNotFoundException(
                    "Unable to find " + PageLayer.class.getName() + " with pageId" + pageId + "and layerId " + layerId);
        }
    }

    public void createPageLayers(List<Long> pagesId, Layer layer) {
        List<PageLayer> pageLayers = new ArrayList<>();
        pagesId.forEach(pageId -> pageLayers.add(pageLayersValidator.create(
                new PageLayerCore(layer.getName(), layer.getDescription(), pageId, layer.getId()), null, null)));
    }

    public void createPageLayersForTranscription(Long pageId, Long transcriptionId) {
        var layerService = BeanLoader.getBean(LayerService.class);
        layerService.getAllLayersPerTranscription(transcriptionId)
                .forEach(layer -> pageLayersValidator.create(
                        new PageLayerCore(layer.getName(), layer.getDescription(), pageId, layer.getId()), null, null));
    }

    public List<Layer> getPageLayers(Long pageId) {
        return pageLayersRepository.findAllByPageId(pageId).stream().map(PageLayer::getLayer).collect(
                Collectors.toList());
    }
    public List<Layer> getPagerLayer(Long pageId,Long layerId) {
        return pageLayersRepository.findByPageIdAndLayerId(pageId,layerId).stream().map(PageLayer::getLayer).collect(
                Collectors.toList());
    }

}
