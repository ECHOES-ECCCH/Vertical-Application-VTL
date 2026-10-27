package pl.psnc.vtl.portal.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;
import pl.psnc.vtl.portal.conf.datetime.LocalDateTimeDeserializer;
import pl.psnc.vtl.portal.conf.datetime.LocalDateTimeSerializer;

import java.time.LocalDateTime;


@UtilityClass
public class TestJsonMapper {
    public ObjectMapper serializingObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

}
