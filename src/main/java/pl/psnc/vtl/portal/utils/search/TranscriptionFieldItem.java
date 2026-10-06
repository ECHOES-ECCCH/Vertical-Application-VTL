package pl.psnc.vtl.portal.utils.search;

import lombok.Data;
import org.jdom2.Namespace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TranscriptionFieldItem {

    public static final String TITLE = "title";
    public static final String CREATOR = "creator";
    public static final String COORDINATOR = "coordinator";
    public static final String COLLECTION = "collection";
    public static final String DESCRIPTION = "description";
    public static final String KEYWORDS = "keywords";
    public static final String DATE = "lastModificationDate";
    public static final String TYPE = "documentTypes";
    public static final String SOURCE = "documentSource";
    public static final String RELEASE_DATE = "documentReleaseDate";
    public static final String FORMAT = "format";
    public static final String IDENTIFIER = "identifier";

    public static final String LINK = "link";
    public static final String IIIF_LINK = "IIIFLink";

    private List<String> content;
    private String name;
    private Namespace namespace;
    private Map<String, String> attributes = new HashMap<>();

    public TranscriptionFieldItem(List<String> content, String name, Namespace namespace) {
        this.content = content;
        this.name = name;
        this.namespace = namespace;
    }

    public TranscriptionFieldItem(List<String> content, String name) {
        this.content = content;
        this.name = name;
    }

    public TranscriptionFieldItem setAttribute(String attributeName, String attributeValue) {
        attributes.put(attributeName, attributeValue);
        return this;
    }

}
