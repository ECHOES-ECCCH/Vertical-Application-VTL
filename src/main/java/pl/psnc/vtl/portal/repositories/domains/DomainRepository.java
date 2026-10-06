package pl.psnc.vtl.portal.repositories.domains;

import lombok.RequiredArgsConstructor;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.reloading.PeriodicReloadingTrigger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.domains.Domain;
import pl.psnc.vtl.portal.model.imports.FileType;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DomainRepository {

    @Value("${vtl.conf-dir}")
    private String filesLocation;

    private final String DOMAINS_FILE_NAME = "domains.properties";

    private ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> builder;

    public List<Domain> getAllowedDomains()
            throws ConfigurationException {
        Configuration configuration = builder.getConfiguration();
        List<Domain> iiifDomains = configuration.getList("iiif.domains").stream()
                .map(v -> new Domain(v.toString(), FileType.IIIF)).collect(Collectors.toList());
        List<Domain> metsDomains = configuration.getList("mets.domains").stream()
                .map(v -> new Domain(v.toString(), FileType.METS)).collect(Collectors.toList());
        List<Domain> teiDomains = configuration.getList("tei.domains").stream()
                .map(v -> new Domain(v.toString(), FileType.TEI)).collect(Collectors.toList());

        List<Domain> allDomains = new ArrayList<>();
        allDomains.addAll(iiifDomains);
        allDomains.addAll(metsDomains);
        allDomains.addAll(teiDomains);
        return allDomains;

    }


    @PostConstruct
    private void loadPropertiesFile() {
        Parameters params = new Parameters();
        builder = new ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration>(
                PropertiesConfiguration.class).configure(
                params.properties().setFileName(filesLocation + DOMAINS_FILE_NAME)
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        PeriodicReloadingTrigger trigger = new PeriodicReloadingTrigger(builder.getReloadingController(), null, 1,
                TimeUnit.MINUTES);
        trigger.start();
    }

}
