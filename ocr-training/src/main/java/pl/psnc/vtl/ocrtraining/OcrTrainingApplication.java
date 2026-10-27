package pl.psnc.vtl.ocrtraining;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import pl.psnc.vtl.ocrtraining.domain.training.OcrTrainingExampleService;
import pl.psnc.vtl.ocrtraining.domain.training.OcrTrainingService;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
public class OcrTrainingApplication {

    @Autowired
    private ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(OcrTrainingApplication.class, args);
    }

    @Bean
    public OpenAPI customOpenAPI(@Value("${springdoc.version}") String apiVersion) {

        return new OpenAPI().info(new Info()
                .title("VTL service for training Tesseract OCR engine")
                .description("Virtual Transcription Laboratory training service "
                        + "accompanied with OCR engines (e.g. tesseract-ocr)")
                .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html"))
                .version(apiVersion));
    }

    @Scheduled(cron = "${vtl.scheduler.run-ocr-training.cron}")
    public void runDailyMidnightOcrTraining() {
        context.getBean(OcrTrainingService.class).resolveAndStartTrainingProcedure();
    }

    @Scheduled(cron = "${vtl.scheduler.fetch-pending-ocr-corrections.cron}")
    public void fetchPendingOcrCorrections() {
        context.getBean(OcrTrainingExampleService.class).processPendingTrainingExamples();
    }
}
