package pl.psnc.vtl.portal.conf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            builder.mixIn(Object.class, IgnoreHibernatePropertiesInJackson.class);
        };
    }

    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private static class IgnoreHibernatePropertiesInJackson {
        // intentionally empty
    }
}
