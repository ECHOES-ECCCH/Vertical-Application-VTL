package pl.psnc.vtl.portal.services.imports.mets;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pl.psnc.vtl.portal.repositories.imports.DocumentsLoaderRepository;
import pl.psnc.vtl.portal.services.imports.LoadedFile;
import pl.psnc.vtl.portal.services.imports.Link;
import pl.psnc.vtl.portal.services.imports.PublicationLinks;
import pl.psnc.vtl.portal.services.imports.iiif.IIIFLinkMapperStrategy;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class LinkedPublicationHandler implements Function<List<Link>, PublicationLinks> {

    private final DocumentsLoaderRepository repository = new DocumentsLoaderRepository();
    private final IIIFLinkMapperStrategy mapper = new IIIFLinkMapperStrategy();

    {
        repository.init();
    }

    @Override
    @SneakyThrows
    public PublicationLinks apply(List<Link> links) {
        String resourceUrl = links.get(0).getUrl();
        Document doc = Jsoup.connect(resourceUrl).get();
        Element manifestLinkElement = Optional.ofNullable(doc.getElementById("lnkManifestIIIF"))
                .orElseThrow(() -> new IllegalArgumentException("Missing obligatory lnkManifestIIIF field."));
        String manifestLink = manifestLinkElement.attr("href");
        String jsonString = repository.loadDocumentStringFromResource(manifestLink);
        LoadedFile dto = new LoadedFile(resourceUrl, jsonString);
        return mapper.getTranscriptionLinks(dto);
    }
}
