package pl.psnc.vtl.portal.services.layers;

import org.mapstruct.factory.Mappers;
import pl.psnc.vtl.portal.dto.layers.PageLayerDto;
import pl.psnc.vtl.portal.model.layers.PageLayer;

import java.util.List;

public interface PageLayerMapper {

    PageLayerMapper INSTANCE = Mappers.getMapper(PageLayerMapper.class);

    PageLayerDto toDto(PageLayer pageLayer);

    List<PageLayerDto> toDto(List<PageLayer> pageLayers);

}
