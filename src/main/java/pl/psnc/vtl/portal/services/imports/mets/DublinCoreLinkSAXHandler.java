package pl.psnc.vtl.portal.services.imports.mets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import pl.psnc.vtl.portal.services.imports.LinkMappingConfiguration;
import pl.psnc.vtl.portal.services.imports.TextFormatValidator;
import pl.psnc.vtl.portal.services.imports.PublicationLinks;
import pl.psnc.vtl.portal.services.imports.Link;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;


public class DublinCoreLinkSAXHandler extends DefaultHandler {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private final class FileGroup{
        String groupMimeType;
        List<File> files = new ArrayList<>();
    }

    @Data
    private final class File{
        String resourceUrl;
        String mimeTYpe;
    }
    private FileGroup currentFileGroup;
    private File currentFile;
    private List<FileGroup> fileGroups = new ArrayList<>();


    private boolean isInsideFileGroup = false;
    private boolean isInsideSimpleFile = false;

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts){
        handleFile(qName, atts);
        handleInsideSimpleFile(qName, atts);
        handleInsideFileGroup(qName);
    }

    private void handleFile(String qName, Attributes atts) {
        if(shouldConsiderLink(qName) && !atts.getValue("xlink:href").endsWith("PresentationData.xml")) currentFile.setResourceUrl(atts.getValue("xlink:href"));
    }

    private void handleInsideFileGroup(String qName) {
        if(isInsideFileGroup) return;
        isInsideFileGroup = qName.equals("fileGrp");
        if(isInsideFileGroup) currentFileGroup = new FileGroup();
    }

    private void handleInsideSimpleFile(String qName, Attributes atts){
        if(isInsideSimpleFile) return;
        isInsideSimpleFile = isInsideFileGroup
                && qName.equals("file")
                && LinkMappingConfiguration.isConsideredMimeType(atts.getValue("MIMETYPE"));
        if (isInsideSimpleFile) {
            currentFile = new File();
            currentFile.setMimeTYpe(atts.getValue("MIMETYPE"));
        }
    }

    public void endElement (String uri, String localName, String qName){
        if(qName.equals("fileGrp")){
            isInsideFileGroup = false;
            if(!currentFileGroup.getFiles().isEmpty())
                fileGroups.add(currentFileGroup);
            currentFileGroup = null;
        }
        if(qName.equals("file")){
            isInsideSimpleFile = false;
            if(currentFile != null) currentFileGroup.getFiles().add(currentFile);
            currentFile = null;
        }

    }

    private BinaryOperator<FileGroup> higherOrderedMimeType = (f1, f2) ->
            LinkMappingConfiguration.getMimeTypeOrder(f1.getGroupMimeType()) >
                    LinkMappingConfiguration.getMimeTypeOrder(f2.getGroupMimeType()) ? f1 : f2;


    private boolean shouldConsiderLink(String qName){
        return qName.equals("FLocat") && isInsideFileGroup && isInsideSimpleFile;
    }

    public PublicationLinks getLinks(){
        FileGroup fileGroup = getMainFileGroup();
        List<Link> links = getLinksFromFiles(fileGroup);
        Function<List<Link>, PublicationLinks> handler = LinkMappingConfiguration.getHandler(fileGroup.getGroupMimeType());
        return handler.apply(links);
    }

    private FileGroup getMainFileGroup() {
        return fileGroups.stream()
                    .map(this::createGroupWithMainMimeType)
                    .filter(group -> group.getGroupMimeType() != null)
                    .reduce(higherOrderedMimeType)
                    .orElseThrow(TextFormatValidator::getUnsupportedTypesException);
    }

    private List<Link> getLinksFromFiles(FileGroup fileGroup) {
        return fileGroup.getFiles().stream()
                    .map(File::getResourceUrl)
                    .sorted()
                    .map(Link::new)
                    .collect(Collectors.toList());
    }


    /**
     * group mime type is the mimetype of the first element of non empty files list
     * assuming that file group has file of the same type
     */

    BinaryOperator<Map.Entry<String, List<File>>> theMostFrequentMimeType = (e1, e2) -> e1.getValue().size() >  e2.getValue().size() ? e1 : e2;

    private FileGroup createGroupWithMainMimeType(FileGroup fileGroup) {
        Map<String, List<File>> occurrenceNumbAndFileListMap = getOccurrenceNumbAndFileListMap(fileGroup);
        Map.Entry<String, List<File>> entry = occurrenceNumbAndFileListMap.entrySet().stream()
                .reduce(theMostFrequentMimeType)
                .orElseThrow(TextFormatValidator::getWrongTypeXMLException);

        return new FileGroup(entry.getKey(), entry.getValue());
    }

    private Map<String, List<File>> getOccurrenceNumbAndFileListMap(FileGroup fileGroup) {
        return fileGroup.files.stream()
                    .collect(Collectors.groupingBy(File::getMimeTYpe, Collectors.toList()));
    }


}
