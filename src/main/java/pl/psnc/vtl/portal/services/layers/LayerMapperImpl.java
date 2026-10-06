package pl.psnc.vtl.portal.services.layers;

import pl.psnc.vtl.portal.dto.layers.LayerDto;
import pl.psnc.vtl.portal.model.layers.Layer;

import java.util.ArrayList;
import java.util.List;

public class LayerMapperImpl implements LayerMapper {

    @Override
    public LayerDto toDto(Layer layer) {
        if (layer == null) {
            return null;
        }

        LayerDto layerDto = new LayerDto();
        layerDto.setDescription(layer.getDescription());
        layerDto.setId(layer.getId());
        layerDto.setName(layer.getName());
        layerDto.setType(layer.getType());

        return layerDto;
    }


    @Override
    public List<LayerDto> toDto(List<Layer> layers) {
        if (layers == null) {
            return null;
        }

        List<LayerDto> list = new ArrayList<>(layers.size());
        for (Layer layer : layers) {
            list.add(toDto(layer));
        }

        return list;
    }
}
