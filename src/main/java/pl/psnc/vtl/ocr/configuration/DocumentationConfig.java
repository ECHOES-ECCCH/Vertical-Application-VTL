package pl.psnc.vtl.ocr.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DocumentationConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${springdoc.version}") String apiVersion) {
        return new OpenAPI().info(new Info().title("VTL tools to call OCR engines tools")
                .description("Virtual Transcription Laboratory tools accompanied with OCR engines (e.g. tesseract-ocr)")
                .version(apiVersion)
                .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
