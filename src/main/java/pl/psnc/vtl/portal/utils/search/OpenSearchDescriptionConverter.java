package pl.psnc.vtl.portal.utils.search;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.portal.conf.OpenSearchProperties;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.services.iiifserver.IIIFUriResolver;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static pl.psnc.vtl.portal.utils.search.Namespaces.*;
import static pl.psnc.vtl.portal.utils.search.OpenSearchDescriptionXmlField.*;

@Data
@Component
@RequiredArgsConstructor
public class OpenSearchDescriptionConverter {

    private final IIIFUriResolver iiifUriResolver;

    private final OpenSearchProperties properties;
    private OpenSearchConverterData data;

    public Document getDocument() {
        Element rss = getRssElement();
        rss.addContent(getChannelWithItems(getItems(data.getDocuments())));
        return new Document().setContent(rss);
    }

    private List<List<Element>> getItems(List<Transcription> docs) {
        List<List<Element>> items = new ArrayList<>();
        docs.forEach(doc -> items.add(getItemFromTranscription(doc)));
        return items;
    }

    private List<Element> getItemFromTranscription(Transcription transcription) {
        String collection = transcription.getTranscriptionInfo().getCollectionName() != null ? transcription.getTranscriptionInfo().getCollectionName() : "";
        String coordinator = transcription.getTranscriptionInfo().getContactName() != null ? transcription.getTranscriptionInfo().getContactName() : "";

        return getElementsFromTranscriptionFields(List.of(
                new TranscriptionFieldItem(List.of(String.valueOf(transcription.getId())), TranscriptionFieldItem.IDENTIFIER, DC),
                new TranscriptionFieldItem(List.of(properties.getIdUrlPrefix() + transcription.getId()), TranscriptionFieldItem.LINK),
                new TranscriptionFieldItem(List.of("IIIF"), TranscriptionFieldItem.FORMAT, DC),
                new TranscriptionFieldItem(List.of(iiifUriResolver.getManifestUrl(properties.getUrlWithPort(), transcription.getId())), TranscriptionFieldItem.IIIF_LINK, DC),
                new TranscriptionFieldItem(List.of(transcription.getAnalyticInfo().getTitle()), TranscriptionFieldItem.TITLE, DC),
                new TranscriptionFieldItem(List.of(transcription.getCreator().getEmail()), TranscriptionFieldItem.CREATOR, DC),
                new TranscriptionFieldItem(List.of(coordinator), TranscriptionFieldItem.COORDINATOR, DC),
                new TranscriptionFieldItem(List.of(collection), TranscriptionFieldItem.COLLECTION, DC),
                new TranscriptionFieldItem(List.of(transcription.getTranscriptionInfo().getDescriptionPlain()), TranscriptionFieldItem.DESCRIPTION, DC),
                new TranscriptionFieldItem(transcription.getProfileDesc().getKeywords(), TranscriptionFieldItem.KEYWORDS, DC),
                new TranscriptionFieldItem(List.of(transcription.getLastModificationDate().toString()), TranscriptionFieldItem.DATE, DC),
                new TranscriptionFieldItem(List.of(transcription.getTranscriptionInfo().getDocumentTypes()), TranscriptionFieldItem.TYPE, DC),
                new TranscriptionFieldItem(List.of(transcription.getTranscriptionInfo().getDocumentSrcPlain()), TranscriptionFieldItem.SOURCE, DC),
                new TranscriptionFieldItem(List.of(transcription.getTranscriptionInfo().getReleaseDate()), TranscriptionFieldItem.RELEASE_DATE, DC)));
    }

    private List<Element> getElementsFromTranscriptionFields(List<TranscriptionFieldItem> transcriptionFieldItems) {
        List<Element> itemElements = new ArrayList<>();
        transcriptionFieldItems.stream().filter(transcriptionFieldItem -> transcriptionFieldItem.getContent() != null).forEach(
                transcriptionFieldItem -> transcriptionFieldItem.getContent().stream().filter(Objects::nonNull)
                        .forEach(content -> itemElements.add(getElementFromTranscriptionFieldItem(transcriptionFieldItem, content))));
        return itemElements;
    }

    private Element getElementFromTranscriptionFieldItem(TranscriptionFieldItem fieldItem, String content) {
        Element element = new Element(fieldItem.getName()).addContent(content);
        if (fieldItem.getNamespace() != null) {
            element.setNamespace(fieldItem.getNamespace());
        }
        fieldItem.getAttributes().forEach(element::setAttribute);
        return element;
    }

    private Element getChannelWithItems(List<List<Element>> items) {
        Element channel = getChannelElement();
        items.forEach(item -> channel.addContent(getItemElement(item)));
        return channel;
    }

    private Element getChannelElement() {
        Element channel = new Element("channel");
        getChannelDefaultElements().forEach(channel::addContent);
        return channel;
    }

    private Element getItemElement(List<Element> itemElement) {
        Element item = new Element("item");
        itemElement.forEach(item::addContent);
        return item;
    }

    private List<Element> getChannelDefaultElements() {
        List<Element> channelElements = new ArrayList<>();

        channelElements.add(new Element(ChannelField.TITLE).addContent("Wyniki wyszukiwania"));

        channelElements.add(new Element(ChannelField.DESCRIPTION).addContent(properties.getDescription()));

        channelElements.add(new Element(ChannelField.LINK).addContent(
                properties.getUrlWithPort() + "/opensearch?searchTerms=" +
                        URLEncoder.encode(data.getQuery(), StandardCharsets.UTF_8)));

        channelElements.add(new Element(ChannelField.LINK, ATOM).setAttribute("rel", "search")
                .setAttribute("type", "application/opensearchdescription+xml")
                .setAttribute("href", properties.getUrlWithPort() + "/opensearch/description.xml"));

        channelElements.add(new Element(ChannelField.LINK, ATOM).setAttribute("rel", "self")
                .setAttribute("type", "application/rss+xml").setAttribute("href",
                        properties.getUrlWithPort() + "/opensearch?searchTerms=" +
                                URLEncoder.encode(data.getQuery(), StandardCharsets.UTF_8)));

        channelElements.add(new Element(ChannelField.SEARCH_TERMS, OPENSEARCH).addContent(data.getQuery()));

        channelElements.add(new Element(ChannelField.START_PAGE, OPENSEARCH).addContent(String.valueOf(data.getPage())));

        channelElements.add(new Element(ChannelField.COUNT, OPENSEARCH).addContent(String.valueOf(data.getPerPage())));

        channelElements.add(new Element(ChannelField.QUERY, OPENSEARCH).setAttribute("role", "request")
                .setAttribute("searchTerms", data.getQuery()));

        return channelElements;
    }

    private Element getRssElement() {
        Element rss = new Element("rss").setAttribute("version", "2.0").setAttribute("schemaLocation",
                properties.getUrlWithPort() + "/opensearch/" + properties.getFbcSearchAddinsFormat() +
                        "/fbc-search-addins." + properties.getFbcSearchAddinsFormat() + "", XSI);
        addNamespacesDeclarationToElement(rss, OPENSEARCH, ATOM, DC);
        return rss;
    }

    private void addNamespacesDeclarationToElement(Element element, Namespace... namespaces) {
        Arrays.stream(namespaces).forEach(element::addNamespaceDeclaration);
    }

    public Document getDescriptionXml() {
        Element osd = new Element("OpenSearchDescription", XMLNS);
        osd.addNamespaceDeclaration(XMLNS);

        List<Element> elements = new ArrayList<>(
                Arrays.asList(getDescriptionXmlElementFromField(SHORT_NAME, properties.getShortName()),
                        getDescriptionXmlElementFromField(DESCRIPTION, properties.getDescriptionSummary()),
                        getDescriptionXmlElementFromField(TAG, properties.getTag()),
                        getDescriptionXmlElementFromField(CONTACT, properties.getContact()),
                        getDescriptionXmlElementFromField(URL).setAttribute("type", "application/xml")
                                .setAttribute("template",
                                        properties.getUrlWithPort() + "/opensearch?searchTerms={searchTerms}&startPage={startPage}&count={count}"),
                        getDescriptionXmlElementFromField(LONG_NAME, properties.getLongName()),
                        getDescriptionXmlElementFromField(IMAGE,
                                properties.getBackendUrl() + "/api/static-files/images/ariadna-logo.svg").setAttribute("height", "64")
                                .setAttribute("width", "64").setAttribute("type", "image/svg"),
                        getDescriptionXmlElementFromField(QUERY).setAttribute("role", "example")
                                .setAttribute("searchTerms", "cat"),
                        getDescriptionXmlElementFromField(DEVELOPER, properties.getDeveloper()),
                        getDescriptionXmlElementFromField(ATTRIBUTION, properties.getAttribution()),
                        getDescriptionXmlElementFromField(SYNDICATION_RIGHT, properties.getSyndicationRight()),
                        getDescriptionXmlElementFromField(ADULT_CONTENT, properties.getAdultContent()),
                        getDescriptionXmlElementFromField(LANGUAGE, properties.getLanguage()),
                        getDescriptionXmlElementFromField(OUTPUT_ENCODING, properties.getOutputEncoding()),
                        getDescriptionXmlElementFromField(INPUT_ENCODING, properties.getInputEncoding())));

        elements.forEach(osd::addContent);
        return new Document().addContent(osd);
    }

    private Element getDescriptionXmlElementFromField(String fieldName) {
        return new Element(fieldName, XMLNS);
    }

    private Element getDescriptionXmlElementFromField(String fieldName, String content) {
        return new Element(fieldName, XMLNS).addContent(content);
    }

}
