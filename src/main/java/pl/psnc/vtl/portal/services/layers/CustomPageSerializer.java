package pl.psnc.vtl.portal.services.layers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pl.psnc.vtl.portal.model.pages.Page;

import java.io.IOException;

public class CustomPageSerializer extends StdSerializer<Page> {

    private static Page page =  new Page();

    protected CustomPageSerializer(Class<Page> t) {
        super(t);
    }

    @SuppressWarnings("unchecked")
    public CustomPageSerializer() {
        this((Class<Page>) page.getClass());
    }

    @Override
    public void serialize(Page page, JsonGenerator paramJsonGenerator,
            SerializerProvider paramSerializerProvider) throws IOException {

        paramJsonGenerator.writeStartObject();
        paramJsonGenerator.writeStringField("id", page.getId().toString());
        paramJsonGenerator.writeEndObject();
    }
}
