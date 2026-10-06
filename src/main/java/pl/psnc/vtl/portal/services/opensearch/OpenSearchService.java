package pl.psnc.vtl.portal.services.opensearch;

import lombok.AllArgsConstructor;
import org.jdom2.Document;
import org.jdom2.output.XMLOutputter;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.utils.search.OpenSearchConverterData;
import pl.psnc.vtl.portal.utils.search.OpenSearchDescriptionConverter;

@AllArgsConstructor
@Service
public class OpenSearchService {

    private final OpenSearchDescriptionConverter converter;

    public String getDocument(OpenSearchConverterData data) {
        converter.setData(data);
        return new XMLOutputter().outputString(converter.getDocument()).trim();
    }

    public String getDescription() {
        Document document = converter.getDescriptionXml();
        return new XMLOutputter().outputString(document).trim();
    }
}
