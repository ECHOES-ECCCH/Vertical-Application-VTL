package pl.psnc.vtl.portal.services.layers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import pl.psnc.vtl.portal.dto.layers.PageLayerCore;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.repositories.layers.LayerRepository;
import pl.psnc.vtl.portal.repositories.pages.PageLayersRepository;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.services.ValidationException;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class PageLayersValidator {

    private final LayerRepository layersRepository;

    private final PageRepository pageRepository;

    private final PageLayersRepository pageLayersRepository;

    public PageLayer create(PageLayerCore pageLayerCore, Long pageId, Long layerId) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(pageLayerCore, "pageLayer");
        PageLayer pageLayer = getOrCreatePageLayer(pageId, layerId);


        if (!Objects.isNull(pageLayerCore.getPageId())) {
            Page page = pageRepository.findById(pageLayerCore.getPageId()).orElse(null);

            if (page == null)
                errors.rejectValue("pageId", "field.required", "Page Id don't exist, but this field is required to create pageLayer.");
            else
                pageLayer.setPage(page);

            if (!Objects.isNull(pageLayerCore.getLayerId())) {
               Layer layer = layersRepository.findById(pageLayerCore.getLayerId()).orElse(null);

                if (layer == null)
                    errors.rejectValue("layerId", "field.required", "Layer Id don't exist, but this field is required to create pageLayer.");
                else
                    pageLayer.setLayer(layer);
            } else {
                errors.rejectValue("layerId", "field.required", "Layer Id is required to create pageLayer.");
            }
        } else {
            errors.rejectValue("pageId", "field.required", "Page Id don't exist, but this field is required to create pageLayer.");
        }


        if(pageLayer.getPercentOfTranscribed()==null)
            pageLayer.setPercentOfTranscribed(0);

        if(pageLayer.getPercentOfVerified()==null)
            pageLayer.setPercentOfVerified(0);

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return pageLayer;
    }


    private PageLayer getOrCreatePageLayer(Long pageId, Long layerId) {
        if (layerId != null) {
            return pageLayersRepository.findByPageIdAndLayerId(pageId, layerId).get();
        } else return new PageLayer();
    }

}
