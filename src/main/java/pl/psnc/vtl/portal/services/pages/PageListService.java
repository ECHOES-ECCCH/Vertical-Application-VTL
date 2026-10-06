package pl.psnc.vtl.portal.services.pages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.dto.pages.PageWithHitCount;
import pl.psnc.vtl.portal.dto.pages.PaginatedPages;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.layers.LayerRepository;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PageListService {

    private final PageRepository pageRepository;

    private final LayerRepository layerRepository;

    public PaginatedPages getPages(Long transcriptionId, int page, int perpage) {
        PageRequest pageable = PageRequest.of(page - 1, perpage, Sort.by(Sort.Direction.ASC, "pageNo"));
        org.springframework.data.domain.Page<Page> pagesPage = pageRepository
                .findByTranscriptionIdOrderByPageNo(transcriptionId, pageable);

        return PaginatedPages.builder().list(pagesPage.getContent()).count(pagesPage.getContent().size())
                .hits(pagesPage.getTotalElements()).page(page).perpage(perpage).pages(pagesPage.getTotalPages())
                .build();
    }

    public void fillPages(List<Transcription> transcriptions, int page, int perpage) {
        List<Long> tIds = transcriptions.stream().map(Transcription::getId).collect(Collectors.toList());
        int fromPageNo = (page - 1) * perpage + 1;
        int toPageNo = page * perpage;
        Map<Long, List<Layer>> layersByT = layerRepository.findAllByTranscriptionIdIn(tIds)
                .stream().collect(Collectors.groupingBy(l -> l.getTranscription().getId()));
        List<Page> allPages = pageRepository.findWithPageLayersByTranscriptionIdAndPageNo(tIds, fromPageNo, toPageNo);
        Map<Long, List<Page>> pagesByT = allPages
                .stream().collect(Collectors.groupingBy(p -> p.getTranscription().getId()));
        Map<Long, Long> pagesCounts = pageRepository.getPagesCounts(tIds)
                .stream().collect(Collectors.toMap(t -> t[0], t -> t[1]));

        for (Transcription t : transcriptions) {
            List<Page> pages = pagesByT.computeIfAbsent(t.getId(), k -> new ArrayList<>());
            pages.sort(comparing(Page::getPageNo));
            t.setPages(PaginatedPages.builder()
                    .list(pages).page(page).perpage(perpage)
                    .count(pages.size()).hits(pagesCounts.getOrDefault(t.getId(), 0L))
                    .build());

            List<Layer> layers = layersByT.computeIfAbsent(t.getId(), k -> new ArrayList<>());
            layers.sort(comparing(Layer::getId));
            t.setLayers(layers);
        }
    }

    public List<Page> getPages(Long transcriptionId) {
        return pageRepository.findByTranscriptionIdOrderByPageNo(transcriptionId);
    }

    public List<PageWithHitCount> searchPages(String q, List<Long> transcriptionsIds) {
        return pageRepository.findByVerseContent(q, transcriptionsIds);
    }
}
