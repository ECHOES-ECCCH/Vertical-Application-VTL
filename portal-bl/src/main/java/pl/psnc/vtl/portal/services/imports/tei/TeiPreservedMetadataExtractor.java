package pl.psnc.vtl.portal.services.imports.tei;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.dom4j.tree.DefaultElement;
import java.util.Optional;

@Slf4j
@UtilityClass
public class TeiPreservedMetadataExtractor {

    private final String XPATH_TEI_HEADER = "//*[local-name()='teiHeader']";

    private final String XPATH_TEI_BODY = "//*[local-name()='body']";

    private static final class NameSpaceCleaner extends VisitorSupport {
        public void visit(Document document) {
            ((DefaultElement) document.getRootElement())
                    .setNamespace(Namespace.NO_NAMESPACE);
            document.getRootElement().additionalNamespaces().clear();
        }
        public void visit(Namespace namespace) {
            namespace.detach();
        }
        public void visit(Attribute node) {
            if (node.toString().contains("xmlns")
                    || node.toString().contains("xsi:")) {
                node.detach();
            }
        }

        public void visit(Element node) {
            if (node instanceof DefaultElement) {
                ((DefaultElement) node).setNamespace(Namespace.NO_NAMESPACE);
            }
        }
    }


    @SneakyThrows
    public String getTeiHeader(String teiXml) {
        Document teiDoc = DocumentHelper.parseText(teiXml);
        teiDoc.accept(new NameSpaceCleaner());
        String teiHeader = Optional.ofNullable(teiDoc.selectSingleNode(XPATH_TEI_HEADER))
                .map(Node::asXML).orElseGet(() -> null);
        return teiHeader;
    }

    @SneakyThrows
    public String getTeiBody(String teiXml) {
        Document teiDoc = DocumentHelper.parseText(teiXml);
        teiDoc.accept(new NameSpaceCleaner());
        String teiHeader = Optional.ofNullable(teiDoc.selectSingleNode(XPATH_TEI_BODY))
                .map(Node::asXML).orElseGet(() -> null);
        return teiHeader;
    }
}
