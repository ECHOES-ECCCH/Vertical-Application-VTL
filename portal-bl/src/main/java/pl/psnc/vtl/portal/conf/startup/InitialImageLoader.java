package pl.psnc.vtl.portal.conf.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialImageLoader {

    private final IIIFToolsRepository iiifToolsRepository;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${spring.jpa.hibernate.ddl-auto:none}")
    private String jpaDdlAuto;


    public void loadProfileData()
            throws IOException, InterruptedException {
        log.debug("Loading " + activeProfile + " images");

        if (jpaDdlAuto.equals("create") || jpaDdlAuto.equals("create-drop")) {
            iiifToolsRepository.clearIiifServer();
        }

        List<Long> transcriptionIds = Arrays.asList(1L, 2L, 3L);
        for (Long transcriptionId : transcriptionIds) {
            loadTranscriptionImages(transcriptionId);
        }
    }


    private void loadTranscriptionImages(Long transcriptionId)
            throws IOException, InterruptedException {
        ClassLoader classLoader = InitialDataLoader.class.getClassLoader();
        PathMatchingResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver(classLoader);
        Resource[] resources = pathResolver
                .getResources("initial-data/profile/" + activeProfile + "-images/t" + transcriptionId + "/*");
        log.debug("number of pages for transcription " + transcriptionId + ": " + resources.length);
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            iiifToolsRepository.savePageImage(transcriptionId, resource.getInputStream(), filename);
            iiifToolsRepository.makePagePyramid(transcriptionId, filename);
        }
    }

}
