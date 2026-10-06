package pl.psnc.vtl.portal.services.imports.mets;

import pl.psnc.vtl.portal.repositories.imports.DocumentsLoaderRepository;
import pl.psnc.vtl.portal.services.imports.LoadedFile;
import pl.psnc.vtl.portal.services.imports.Link;
import pl.psnc.vtl.portal.services.imports.PublicationLinks;
import pl.psnc.vtl.portal.services.imports.iiif.IIIFLinkMapperStrategy;

import java.util.List;
import java.util.function.Function;

public class ManifestHandler implements Function<List<Link>, PublicationLinks> {

    private final DocumentsLoaderRepository repository = new DocumentsLoaderRepository();
    private final IIIFLinkMapperStrategy mapper = new IIIFLinkMapperStrategy();

    {
        repository.init();
    }

    @Override
    public PublicationLinks apply(List<Link> links) {
        String resourceUrl = links.get(0).getUrl();
        String jsonSting = repository.loadDocumentStringFromResource(resourceUrl);
        LoadedFile dto = new LoadedFile(resourceUrl, jsonSting);
        return mapper.getTranscriptionLinks(dto);
    }
}
