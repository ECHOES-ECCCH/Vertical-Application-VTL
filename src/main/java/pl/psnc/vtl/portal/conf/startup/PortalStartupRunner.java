package pl.psnc.vtl.portal.conf.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.portal.conf.BuildInfo;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortalStartupRunner implements CommandLineRunner {
    private static boolean appLoaded = false;

    public static boolean isAppLoaded() {
        return appLoaded;
    }

    private final InitialDataLoader initialDataLoader;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;


    @Override
    public void run(String... args) throws Exception {
        Date start = new Date();

        String[] profiles = activeProfile.split("-");
        initialDataLoader.loadBasicData();

        if (profiles[0].equals("test")) {
            initialDataLoader.loadProfileData("dev", "test");
        } else {
            initialDataLoader.loadProfileData(profiles[0]);
        }

        Date stop = new Date();
        double time = ((double) (stop.getTime() - start.getTime())) / 1000;
        log.info("Initialized in " + time + " seconds");
        appLoaded = true;
    }


}
