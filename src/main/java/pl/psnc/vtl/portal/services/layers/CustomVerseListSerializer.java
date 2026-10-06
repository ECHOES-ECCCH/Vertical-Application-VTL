package pl.psnc.vtl.portal.services.layers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pl.psnc.vtl.portal.model.verses.Verse;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomVerseListSerializer extends StdSerializer<List<Verse>> {

    private static List<Verse> pageLayerList = new ArrayList<Verse>();

    protected CustomVerseListSerializer(Class<List<Verse>> t) {
        super(t);
    }

    @SuppressWarnings("unchecked")
    public CustomVerseListSerializer() {
        this((Class<List<Verse>>) pageLayerList.getClass());
    }

    @Override
    public void serialize(List<Verse> verseList, JsonGenerator paramJsonGenerator,
            SerializerProvider paramSerializerProvider) throws IOException {

        paramJsonGenerator.writeStartArray();;
        verseList.forEach(verse -> {
            try {
                paramJsonGenerator.writeNumber(verse.getId());
            } catch (IOException e) {
                throw new EntityNotFoundException("");
            }
        });
        paramJsonGenerator.writeEndArray();
    }
}
