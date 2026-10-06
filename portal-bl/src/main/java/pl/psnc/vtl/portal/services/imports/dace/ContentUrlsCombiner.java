package pl.psnc.vtl.portal.services.imports.dace;

import lombok.RequiredArgsConstructor;
import pl.psnc.vtl.portal.services.imports.LinkMappingConfiguration;

import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;

@RequiredArgsConstructor
public class ContentUrlsCombiner implements BinaryOperator<List<String>> {
    private final Function<List<String>, String> determineContentTypeFromStrings;
    @Override
    public List<String> apply(List<String> strings, List<String> strings2) {
        String newLinkMimeType = determineContentTypeFromStrings.apply(strings2);
        String linksMimeType = determineContentTypeFromStrings.apply(strings);
        if (LinkMappingConfiguration.getMimeTypeOrder(newLinkMimeType) <
                LinkMappingConfiguration.getMimeTypeOrder(linksMimeType)) {
            return strings2;
        } else if (Objects.equals(LinkMappingConfiguration.getMimeTypeOrder(newLinkMimeType),
                LinkMappingConfiguration.getMimeTypeOrder(linksMimeType))) {
            strings.addAll(strings2);
        }
        return strings;
    }
}
