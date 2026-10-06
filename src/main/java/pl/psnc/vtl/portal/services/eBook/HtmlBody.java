package pl.psnc.vtl.portal.services.eBook;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.psnc.vtl.portal.model.indexes.Index;
import pl.psnc.vtl.portal.model.indexes.IndexType;
import pl.psnc.vtl.portal.model.verses.Verse;

import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class HtmlBody {

    /** 1, 2 or 3 */
    private final int sectionLevel;

    private final String content;

    private final Map<String, String> pagesIiifUrls;

    private final List<IndexAnchor> indexAnchors;

    @Getter
    @RequiredArgsConstructor
    public static class IndexAnchor {
        private final String id;
        private final IndexType type;
        private final String name;
        private final int pageNo;
        private final int verseNo;

        public IndexAnchor(String id, Index index, Verse verse) {
            this(id, index.getType(), index.getName(), verse.getPage().getPageNo(), verse.getVerseNo());
        }
    }
}
