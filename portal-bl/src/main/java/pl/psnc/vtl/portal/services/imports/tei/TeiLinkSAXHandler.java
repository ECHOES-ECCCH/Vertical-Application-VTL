package pl.psnc.vtl.portal.services.imports.tei;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import pl.psnc.vtl.portal.services.imports.LinkMappingConfiguration;
import pl.psnc.vtl.portal.services.imports.PublicationLinks;
import pl.psnc.vtl.portal.services.imports.Link;

import java.net.URL;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


public class TeiLinkSAXHandler extends DefaultHandler {

    private static final String MANUS_IMAGE_PREFIX = "image";

    private List<String> imageLinks = new LinkedList<>();

    private boolean isInsideText = false;
    private boolean isInsideBody = false;
    private boolean isIdno = false;
    private String teiIdno = null;
    private  String repositoryUrl = "";

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts){
        isIdno = localName.equals("idno");
        if(shouldConsiderLink(localName)) {
            imageLinks.add(atts.getValue("facs"));
        }
        handleInsideBody(localName);
        handleInsideText(localName);
    }

    private void handleInsideText(String localName) {
        if(isInsideText) return;
        isInsideText = localName.equals("text");
    }

    private void handleInsideBody(String localName){
        if(isInsideBody) return;
        isInsideBody = isInsideText
                && localName.equals("body");
    }

    public void endElement (String uri, String localName, String qName){
        if (localName.equals("text")) isInsideText = false;
    }

    @Override
    public void characters(char[] ch, int start, int length){
        if (isIdno) {
            teiIdno = new String(ch, start, length);
            isIdno = false;
        }
    }

    private boolean shouldConsiderLink(String localName){
        return localName.equals("pb") && isInsideText && isInsideBody;
    }


    private String extractFileName(String url) {
        return Paths.get(url).getFileName().toString();
    }

    private String makeUrlAbsolute(String url){
        StringBuilder sb = new StringBuilder();
        if (!url.contains(":")) {
            if (!StringUtils.isEmpty(teiIdno)) {
                sb.append(repositoryUrl);
                sb.append("/");
                sb.append(MANUS_IMAGE_PREFIX);
                sb.append("/");
                sb.append(teiIdno);
                sb.append("/");
            }
            url = extractFileName(url);
        }
        sb.append(url);
        return  sb.toString();
    }

    public PublicationLinks getLinks(){
        List<Link> links = imageLinks
                .stream()
                .map(this::makeUrlAbsolute)
                .filter(LinkMappingConfiguration::isValidFileExtension)
                .map(Link::new)
                .collect(Collectors.toList());
        return new PublicationLinks(links, false);
    }

    public void setRepositoryUrl(String repositoryUrl){
        try {
            URL validatedUrl = new URL(repositoryUrl);
            this.repositoryUrl = String.format("%s://%s", validatedUrl.getProtocol(), validatedUrl.getHost());
        } catch (Exception ignore) {}
    }

}
