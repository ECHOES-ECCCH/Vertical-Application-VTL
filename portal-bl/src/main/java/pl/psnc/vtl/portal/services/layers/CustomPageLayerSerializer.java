package pl.psnc.vtl.portal.services.layers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pl.psnc.vtl.portal.model.layers.PageLayer;

import java.io.IOException;

public class CustomPageLayerSerializer extends StdSerializer<PageLayer> {
    private static PageLayer pageLayer = new PageLayer();

    protected CustomPageLayerSerializer(Class<PageLayer> t) {
        super(t);
    }

    @SuppressWarnings("unchecked")
    public CustomPageLayerSerializer() {
        this((Class<PageLayer>) pageLayer.getClass());
    }

    @Override
    public void serialize(PageLayer pageLayer, JsonGenerator paramJsonGenerator,
            SerializerProvider paramSerializerProvider) throws IOException {

        paramJsonGenerator.writeStartObject();
        paramJsonGenerator.writeStringField("pageId", pageLayer.getPage().getId().toString());
        paramJsonGenerator.writeStringField("layerId", pageLayer.getLayer().getId().toString());
        paramJsonGenerator.writeStringField("percentOfTranscribed", pageLayer.getPercentOfTranscribed().toString());
        paramJsonGenerator.writeStringField("percentOfVerified", pageLayer.getPercentOfVerified().toString());
        paramJsonGenerator.writeStringField("addedOn", pageLayer.getAddedOn().toString());
        paramJsonGenerator.writeEndObject();
    }
}

