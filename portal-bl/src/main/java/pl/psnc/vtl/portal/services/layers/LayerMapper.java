package pl.psnc.vtl.portal.services.layers;

import org.mapstruct.factory.Mappers;
import pl.psnc.vtl.portal.dto.layers.LayerDto;
import pl.psnc.vtl.portal.model.layers.Layer;

import java.util.List;

public interface LayerMapper {

    LayerMapper INSTANCE = Mappers.getMapper(LayerMapper.class);

    LayerDto toDto(Layer layer);

    List<LayerDto> toDto(List<Layer> layers);

}
