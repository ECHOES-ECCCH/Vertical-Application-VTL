package pl.psnc.vtl.portal.services.layers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pl.psnc.vtl.portal.model.layers.Layer;

import javax.persistence.EntityNotFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomLayerListSerializer extends StdSerializer<List<Layer>> {

    private static final List<Layer> layerList = new ArrayList<>(1);

    protected CustomLayerListSerializer(Class<List<Layer>> t) {
        super(t);
    }

    @SuppressWarnings("unchecked")
    public CustomLayerListSerializer() {
        this((Class<List<Layer>>) layerList.getClass());
    }


    @Override
    public void serialize(List<Layer> layers, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();
        layers.forEach(layer -> {
            try {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("id", layer.getId().toString());
                jsonGenerator.writeStringField("name", layer.getName());
                jsonGenerator.writeObjectField("type", layer.getType());
                jsonGenerator.writeEndObject();
            } catch (IOException e) {
                throw new EntityNotFoundException("");
            }
        });
        jsonGenerator.writeEndArray();
    }
}
