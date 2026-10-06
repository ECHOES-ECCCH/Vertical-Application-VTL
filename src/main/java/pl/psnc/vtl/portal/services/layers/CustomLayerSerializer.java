package pl.psnc.vtl.portal.services.layers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pl.psnc.vtl.portal.model.layers.Layer;

import java.io.IOException;

public class CustomLayerSerializer extends StdSerializer<Layer>  {
    private static Layer layer =  new Layer();

    protected CustomLayerSerializer(Class<Layer> t) {
        super(t);
    }

    @SuppressWarnings("unchecked")
    public CustomLayerSerializer() {
        this((Class<Layer>) layer.getClass());
    }

    @Override
    public void serialize(Layer layer, JsonGenerator paramJsonGenerator,
            SerializerProvider paramSerializerProvider) throws IOException {

        paramJsonGenerator.writeStartObject();
        paramJsonGenerator.writeStringField("id", layer.getId().toString());
        paramJsonGenerator.writeStringField("name", layer.getName());
        paramJsonGenerator.writeObjectField("type", layer.getType());
        paramJsonGenerator.writeEndObject();
    }
}
