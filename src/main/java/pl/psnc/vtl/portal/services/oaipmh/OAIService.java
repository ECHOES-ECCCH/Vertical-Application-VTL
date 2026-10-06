package pl.psnc.vtl.portal.services.oaipmh;

import com.lyncode.xml.exceptions.XmlWriteException;
import org.dspace.xoai.dataprovider.DataProvider;
import org.dspace.xoai.dataprovider.exceptions.OAIException;
import org.dspace.xoai.dataprovider.model.Context;
import org.dspace.xoai.dataprovider.model.MetadataFormat;
import org.dspace.xoai.dataprovider.parameters.OAIRequest;
import org.dspace.xoai.dataprovider.repository.Repository;
import org.dspace.xoai.dataprovider.repository.RepositoryConfiguration;
import org.dspace.xoai.model.oaipmh.DeletedRecord;
import org.dspace.xoai.model.oaipmh.Granularity;
import org.dspace.xoai.model.oaipmh.OAIPMH;
import org.dspace.xoai.services.api.ResumptionTokenFormat;
import org.dspace.xoai.services.impl.SimpleResumptionTokenFormat;
import org.dspace.xoai.xml.XmlWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.repositories.oaipmh.OaiItemRepository;
import pl.psnc.vtl.portal.repositories.oaipmh.OaiSetRepository;

import javax.annotation.PostConstruct;
import javax.xml.stream.XMLStreamException;

import java.io.ByteArrayOutputStream;
import java.util.Date;

@Service
@Transactional(readOnly = true)
public class OAIService {
    protected static final String FORMAT = "oai_dc";

    // Repositories
    private final OaiItemRepository oaiItemRepository;
    private final OaiSetRepository oaiSetRepository;

    // Configuration variables
    private final String repositoryName;
    private final String[] adminEmails;
    private final String repositoryBaseUrl;
    private final int repositoryMaxListIdentifiers;
    private final int repositoryMaxListRecords;
    private final String description;

    // OAI-PHM provider specific structure
    protected DataProvider dataProvider;


    @Autowired
    public OAIService(OaiItemRepository oaiItemRepository, OaiSetRepository oaiSetRepository,
            @Value("${oai-repository.name}") String oaiRepositoryName,
            @Value("${oai-repository.adminEmails}") String[] oaiRepositoryAdminEmails,
            @Value("${oai-repository.baseUrl}") String oaiRepositoryBaseUrl,
            @Value("${oai-repository.max.list.identifiers}") int oaiRepositoryMaxListIdentifiers,
            @Value("${oai-repository.max.list.records}") int oaiRepositoryMaxListRecords,
            @Value("${oai-repository.description}") String description) {
        this.oaiItemRepository = oaiItemRepository;
        this.oaiSetRepository = oaiSetRepository;
        this.repositoryName = oaiRepositoryName;
        this.adminEmails = oaiRepositoryAdminEmails;
        this.repositoryBaseUrl = oaiRepositoryBaseUrl;
        this.repositoryMaxListIdentifiers = oaiRepositoryMaxListIdentifiers;
        this.repositoryMaxListRecords = oaiRepositoryMaxListRecords;
        this.description = description;
    }

    @PostConstruct
    public void setupOaiProvider() {
        ResumptionTokenFormat resumptionTokenFormat = new SimpleResumptionTokenFormat();
        RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration()
                .withRepositoryName(repositoryName)
                .withAdminEmails(adminEmails)
                .withBaseUrl(repositoryBaseUrl)
                .withMaxListIdentifiers(repositoryMaxListIdentifiers)
                .withMaxListRecords(repositoryMaxListRecords)
                // we do not support sets, but needs to be set because otherwise XOAI raises error
                .withMaxListSets(100)
                .withEarliestDate(setupProviderMinDate())
                .withGranularity(Granularity.Day)
                .withDeleteMethod(DeletedRecord.NO)
                .withDescription(description);
        Context context = new Context().withMetadataFormat(FORMAT, MetadataFormat.identity());
        Repository repository = new Repository()
                .withConfiguration(repositoryConfiguration)
                .withSetRepository(oaiSetRepository)
                .withItemRepository(oaiItemRepository)
                .withResumptionTokenFormatter(resumptionTokenFormat);
        this.dataProvider = new DataProvider(context, repository);
    }

    public String handle(OAIRequest oaiRequest) throws OAIException, XMLStreamException, XmlWriteException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        XmlWriter xmlWriter = new XmlWriter(byteOutputStream);
        OAIPMH oaipmhResponse = dataProvider.handle(oaiRequest);
        oaipmhResponse.write(xmlWriter);
        xmlWriter.flush();
        return byteOutputStream.toString();
    }

    private Date setupProviderMinDate() {
        return oaiItemRepository.getMinDate().orElse(new Date());
    }
}
