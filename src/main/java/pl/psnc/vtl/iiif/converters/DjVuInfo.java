package pl.psnc.vtl.iiif.converters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Getter
@AllArgsConstructor
@Slf4j
public class DjVuInfo {
    private static final Pattern indirectSinglePagePattern = Pattern.compile("[ \t]*FORM:DJVU.+", Pattern.DOTALL);
    private static final Pattern isBundledCompositePattern = Pattern.compile(".*FORM:DJVM.+DIRM.+bundled.+",
            Pattern.DOTALL);
    private static final Pattern indirectCompositePattern = Pattern.compile(".*FORM:DJVM.+DIRM.+indirect.+",
            Pattern.DOTALL);

    private boolean indirectSinglePage;
    private boolean isBundledComposite;
    private boolean isIndirectComposite;
    private boolean hasContent;

    public static DjVuInfo parse(String output) {
        log.debug("Processing output {}", output);

        boolean indirectSinglePage = indirectSinglePagePattern.matcher(output).matches();
        boolean bundledComposite = isBundledCompositePattern.matcher(output).matches();
        boolean indirectComposite = indirectCompositePattern.matcher(output).matches();

        log.debug("Indirect single pattern: {}", indirectSinglePage);
        log.debug("Bundled composite pattern: {}", bundledComposite);
        log.debug("Indirect composite pattern: {}", indirectComposite);

        return new DjVuInfo(indirectSinglePage, bundledComposite, indirectComposite, indirectSinglePage || bundledComposite);
    }
}
