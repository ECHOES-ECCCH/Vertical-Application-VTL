package pl.psnc.vtl.portal.services.transcriptions.content;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.portal.dto.blocks.BlockDto;
import pl.psnc.vtl.portal.model.blocks.BlockType;
import pl.psnc.vtl.portal.model.indexes.IndexType;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.services.blocks.BlockService;
import pl.psnc.vtl.portal.services.eBook.HtmlBody;
import pl.psnc.vtl.portal.services.eBook.HtmlBody.IndexAnchor;
import pl.psnc.vtl.portal.services.exports.mappers.HtmlMapper;
import pl.psnc.vtl.portal.services.exports.utils.BlockDtoUtil;
import pl.psnc.vtl.portal.services.iiifserver.IIIFUriResolver;
import pl.psnc.vtl.portal.services.imports.BeanLoader;
import pl.psnc.vtl.portal.services.verses.VerseService;

import javax.persistence.EntityManager;

import java.text.Collator;
import java.util.*;

import static java.lang.Character.toUpperCase;
import static java.util.stream.Collectors.*;
import static pl.psnc.vtl.portal.dto.blocks.BlockDto.onlyBlocks;

@Component
@RequiredArgsConstructor
public class SectionHTMLTranscriptionContentGenerator {

    private final BlockService blockService;

    private final PageRepository pageRepository;

    private final EntityManager entityManager;

    private final IIIFUriResolver iiifUriResolver;

    private void getHeader(String title, StringBuilder sb) {
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<title>" + title + "</title>\n"
                + "</head>\n"
                + "<body>\n");
    }

    public List<HtmlBody> getHTMLBody(Transcription transcription, String ebookTitle, Layer layer) {
        List<Page> pages = pageRepository.findByTranscriptionIdOrderByPageNo(transcription.getId());
        var verseService = BeanLoader.getBean(VerseService.class);

        List<Object> blocks = new ArrayList<>();
        List<Object> annotations = new ArrayList<>();
        int prevPageStartIndex = 0;

        for (Page page : pages) {
            entityManager.detach(page);
            List<Verse> verses = verseService.completeVerses(page);
            page.setVerses(verses);
            ArrayList<Object> pageBlocks = new ArrayList<>(blockService.getBlocksForExport(page, layer));
            putAnnotationsAside(pageBlocks, annotations);
            prevPageStartIndex += mergeBlocks(blocks.subList(prevPageStartIndex, blocks.size()), pageBlocks);
        }

        List<HtmlBody> bodies = new ArrayList<>();
        int chapterNo = 1;
        int itemIndex = 0;
        while (itemIndex < blocks.size()) {
            int sectionLevel = getSectionLevel(blocks.get(itemIndex));
            int nextSectionIndex = itemIndex + 1;
            while (nextSectionIndex < blocks.size() && getSectionLevel(blocks.get(nextSectionIndex)) == 0)
                nextSectionIndex++;
            List<Object> sectionItems = new ArrayList<>(blocks.subList(itemIndex, nextSectionIndex));
            bodies.add(createSectionBody(sectionItems, annotations, sectionLevel, chapterNo++, ebookTitle));
            itemIndex = nextSectionIndex;
        }
        return bodies;
    }

    private int mergeBlocks(List<Object> prevPageItems, List<Object> pageItems) {
        BlockDto continuation = onlyBlocks(pageItems).stream().filter(b -> b.isContinuation()).findFirst().orElse(null);
        BlockDto continued = continuation == null ? null : onlyBlocks(prevPageItems).stream()
                .filter(b -> b.getBlockId() != null) // skip dummy from blockService.getBlocksForExport
                .filter(b -> b.getType() == continuation.getType())
                .reduce((b1, b2) -> b2).orElse(null);
        if (continuation == null || continued == null) {
            prevPageItems.addAll(pageItems);
            return prevPageItems.size() - pageItems.size();
        }

        int continuationIndex = pageItems.indexOf(continuation);
        int continuedIndex = prevPageItems.indexOf(continued);

        prevPageItems.subList(continuedIndex + 1, prevPageItems.size()).clear();
        mergeBlocks(continued.getChildren(), continuation.getChildren());
        prevPageItems.addAll(pageItems.subList(continuationIndex + 1, pageItems.size()));

        return continuedIndex;
    }

    private HtmlBody createSectionBody(List<Object> items, List<Object> annotations, int sectionLevel, int chapterNo,
            String ebookTitle) {
        StringBuilder sb = new StringBuilder();
        List<IndexAnchor> indexAnchors = new ArrayList<>();
        getHeader("Chapter " + chapterNo, sb);
        if (sectionLevel == 0) {
            sb.append("<h1>" + ebookTitle + "</h1>\n");
            sectionLevel = 1;
        }
        for (Object item : items) {
            HtmlMapper.toHtml(item, sb, indexAnchors);
        }
        if (CollectionUtils.isNotEmpty(annotations)) {
            sb.append("<hr>\n");
            for (Object annotation : annotations)
                HtmlMapper.toHtml(annotation, sb, indexAnchors);
        }
        sb.append("</body></html>");
        return new HtmlBody(sectionLevel, sb.toString(), getPagesIiifUrls(items), indexAnchors);
    }

    private void putAnnotationsAside(List<Object> items, List<Object> annotations) {
        for (var it = items.iterator(); it.hasNext();) {
            Object item = it.next();
            if (!(item instanceof BlockDto))
                continue;
            BlockDto block = (BlockDto) item;
            if (block.getType() == BlockType.ANNOTATION) {
                it.remove();
                annotations.add(block);
            } else {
                putAnnotationsAside(block.getChildren(), annotations);
            }
        }
    }

    private HashMap<String, String> getPagesIiifUrls(List<Object> items) {
        HashMap<String, String> pagesIiifUrls = new HashMap<>();
        for (Object item : items) {
            if (item instanceof Verse) {
                if (((Verse) item).isImage()) {
                    String url = iiifUriResolver.getPublicImageUrl((Verse) item);
                    pagesIiifUrls.put(getFilenameFromUrl(url), url);
                }
            } else {
                BlockDtoUtil.allVerses((BlockDto) item).stream().filter(Verse::isImage).forEach(v -> {
                    String url = iiifUriResolver.getPublicImageUrl((Verse) item);
                    pagesIiifUrls.put(getFilenameFromUrl(url), url);
                });
            }
        }
        return pagesIiifUrls;
    }

    private int getSectionLevel(Object item) {
        if (item instanceof BlockDto) {
            BlockType bt = ((BlockDto) item).getType();
            return bt == BlockType.HEADING_1 ? 1
                    : bt == BlockType.HEADING_2 ? 2
                    : bt == BlockType.HEADING_3 ? 3
                    : 0;
        }
        return 0;
    }

    private String getFilenameFromUrl(String url) {
        String[] splitedUrl = url.split("/");
        String filename = splitedUrl[splitedUrl.length - 5];
        String coordinates = splitedUrl[splitedUrl.length - 4].replace(",", "_");
        return filename.replace(".", "_") + "_" + coordinates + ".jpg";
    }

    public String prepareIndexBody(Map<IndexAnchor, String> indexAnchors, String indexLanguage) {
        Locale locale = new Locale(indexLanguage);
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        Comparator<IndexAnchor> comparator = Comparator.comparing(IndexAnchor::getName, Collator.getInstance(locale))
                .thenComparing(IndexAnchor::getPageNo).thenComparing(IndexAnchor::getVerseNo);
        StringBuilder sb = new StringBuilder();
        getHeader(bundle.getString("ebookIndex.headerTitle"), sb);

        EnumMap<IndexType, List<IndexAnchor>> anchorsByType = indexAnchors.keySet().stream()
                .collect(groupingBy(IndexAnchor::getType, () -> new EnumMap<>(IndexType.class), toList()));
        if (anchorsByType.size() > 1)
            sb.append("<h1>" + bundle.getString("ebookIndex.headerTitle") + "</h1>\n");
        int h = indexAnchors.size() > 1 ? 2 : 1;

        anchorsByType.forEach((type, typeAnchors) -> {
            String title = bundle.getString("ebookIndex.sectionTitle." + type.name());
            sb.append("<h" + h + ">" + title + "</h" + h + ">\n");
            typeAnchors.stream()
                    .sorted(comparator)
                    .collect(groupingBy(this::toFirstLetter, LinkedHashMap::new, toList()))
                    .forEach((firstLetter, letterAnchors) -> {
                        sb.append("<h" + (h + 1) + ">" + firstLetter + "</h" + (h + 1) + ">\n");
                        letterAnchors.stream()
                                .collect(groupingBy(IndexAnchor::getName, LinkedHashMap::new, toList()))
                                .forEach((name, nameAnchors) -> {
                                    sb.append("<p>" + StringEscapeUtils.escapeHtml4(name) + " ");
                                    sb.append(nameAnchors.stream()
                                            .map(anchor -> indexLink(anchor, indexAnchors.get(anchor)))
                                            .collect(joining(", ")));
                                    sb.append("</p>\n");
                                });
                    });
        });

        sb.append("</body></html>");
        return sb.toString();
    }

    private char toFirstLetter(IndexAnchor anchor) {
        String name = anchor.getName();
        if (name == null || name.isEmpty())
            return '-';
        char upperCase = toUpperCase(name.charAt(0));
        if (!Character.isLetterOrDigit(upperCase))
            return '-';
        return upperCase;
    }

    private String indexLink(IndexAnchor anchor, String targetFile) {
        return "<a href=\"" + targetFile + "#" + anchor.getId() + "\">" + anchor.getPageNo() + "</a>";
    }
}
