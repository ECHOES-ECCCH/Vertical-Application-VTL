package pl.psnc.vtl.portal.services.layers;

import pl.psnc.vtl.portal.dto.layers.PageLayerDto;
import pl.psnc.vtl.portal.model.layers.PageLayer;

import java.util.ArrayList;
import java.util.List;

public class PageLayerMapperImpl implements PageLayerMapper {

    @Override
    public PageLayerDto toDto(PageLayer pageLayer) {
        if (pageLayer == null) {
            return null;
        }

        PageLayerDto pageLayerDto = new PageLayerDto();
        pageLayerDto.setLayerId(pageLayer.getLayer().getId());
        pageLayerDto.setPageId(pageLayer.getPage().getId());
        pageLayerDto.setPercentOfTranscribed(pageLayer.getPercentOfTranscribed());
        pageLayerDto.setPercentOfVerified(pageLayer.getPercentOfVerified());
        pageLayerDto.setVerses(pageLayer.getVerses());

        return pageLayerDto;
    }


    @Override
    public List<PageLayerDto> toDto(List<PageLayer> pageLayers) {
        if (pageLayers == null) {
            return null;
        }

        List<PageLayerDto> list = new ArrayList<>(pageLayers.size());
        for (PageLayer pageLayer : pageLayers) {
            list.add(toDto(pageLayer));
        }

        return list;
    }
}
