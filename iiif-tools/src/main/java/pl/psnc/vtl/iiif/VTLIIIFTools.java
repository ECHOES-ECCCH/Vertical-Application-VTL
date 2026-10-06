package pl.psnc.vtl.iiif;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.psnc.vtl.iiif.eBooks.ScheduledCleanUpProcess;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class VTLIIIFTools {

    @Bean
    public OpenAPI customOpenAPI(@Value("${springdoc.version}") String apiVersion) {
        return new OpenAPI().info(new Info().title("VTL tools to manage pyramid TIFFs ").description(
                "Virtual Transcription Laboratory tools accompanied with IIPImage Server (e.g. vipslib) working on IIIF Server")
                .version(apiVersion)
                .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    @Bean
    public ScheduledCleanUpProcess scheduledCleanUpProcess(@Value("${vtl.iiif-tools.storage}") String rootFolder) {
        return new ScheduledCleanUpProcess(rootFolder);
    }


    public static void main(String[] args) {
        SpringApplication.run(VTLIIIFTools.class, args);
    }

}

