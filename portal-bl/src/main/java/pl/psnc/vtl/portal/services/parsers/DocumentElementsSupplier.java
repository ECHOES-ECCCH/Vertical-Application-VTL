package pl.psnc.vtl.portal.services.parsers;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.utils.VerseNumberSupplier;

public abstract class DocumentElementsSupplier {
    protected  VerseNumberSupplier verseNumberSupplier;

    public abstract Elements supplyLines(Element element);

    public abstract Verse createVerseWithContentAndBboxOnly(Element element);

    public abstract BoundingBox prepareBoundingBoxForElement(Element element);
}
