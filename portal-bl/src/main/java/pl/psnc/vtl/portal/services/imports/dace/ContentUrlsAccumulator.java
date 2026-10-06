package pl.psnc.vtl.portal.services.imports.dace;

import lombok.RequiredArgsConstructor;
import pl.psnc.vtl.portal.services.imports.LinkMappingConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@RequiredArgsConstructor
public class ContentUrlsAccumulator implements BiFunction<List<String>, String, List<String>> {
    private final Function<String, String> determineContentTypeFromString;
    private final Function<List<String>, String> determineContentTypeFromStrings;

    @Override
    public List<String> apply(List<String> strings, String s) {
        if (strings.isEmpty()) {
            if (LinkMappingConfiguration.isValidFileExtension(s) &&
                    LinkMappingConfiguration.isConsideredMimeType(determineContentTypeFromString.apply(s))) {
                strings.add(s);
            }
            return strings;
        }

        String newLinkMimeType = determineContentTypeFromString.apply(s);
        String linksMimeType = determineContentTypeFromStrings.apply(strings);

        if (LinkMappingConfiguration.isConsideredMimeType(newLinkMimeType)) {
            if (LinkMappingConfiguration.getMimeTypeOrder(newLinkMimeType) <
                    LinkMappingConfiguration.getMimeTypeOrder(linksMimeType)) {
                return new ArrayList<>(List.of(newLinkMimeType));
            } else if (Objects.equals(LinkMappingConfiguration.getMimeTypeOrder(newLinkMimeType),
                    LinkMappingConfiguration.getMimeTypeOrder(linksMimeType))) {
                strings.add(s);
            }
        }
        return strings;
    }
}
