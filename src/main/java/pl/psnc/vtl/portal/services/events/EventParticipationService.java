package pl.psnc.vtl.portal.services.events;

import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.iiif.model.openannotation.Annotation;
import de.digitalcollections.iiif.model.search.SearchHit;
import de.digitalcollections.iiif.model.search.SearchResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.model.events.Event;
import pl.psnc.vtl.portal.model.events.EventAttendee;
import pl.psnc.vtl.portal.model.events.EventTeamPage;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.pages.projections.PageIdAndPageNo;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.repositories.events.EventAttendeeRepository;
import pl.psnc.vtl.portal.repositories.events.EventTeamPageRepository;
import pl.psnc.vtl.portal.repositories.iiifserver.IIIFServerRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.services.blocks.BlockService;
import pl.psnc.vtl.portal.services.iiifpresentation.IIIFPresentation2Service;
import pl.psnc.vtl.portal.services.iiifserver.IIIFUriResolver;
import pl.psnc.vtl.portal.services.imports.BeanLoader;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;
import pl.psnc.vtl.portal.services.verses.VerseService;

import javax.persistence.EntityNotFoundException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
@RequiredArgsConstructor
public class EventParticipationService {

    private final LoggedInUserHolder loggedInUserHolder;
    private final EventAttendeeCache eventAttendeeCache;

    private final EventAttendeeRepository eventAttendeeRepository;
    private final EventTeamPageRepository eventTeamPageRepository;
    private final IIIFServerRepository iiifServerRepository;
    private final VerseRepository verseRepository;

    private final IIIFUriResolver iiifUriResolver;

    private EventAttendee getEventAttendee(long eventId) {
        User user = loggedInUserHolder.getLoggedInUser();
        if (user == null) {
            throw new AccessDeniedException("User not logged in");
        }
        EventAttendee ea = eventAttendeeRepository.findByEventIdAndUser(eventId, user);
        if (ea == null) {
            throw new EntityNotFoundException("User not registered for event");
        }
        if (ea.getTeam() == null) {
            throw new EntityNotFoundException("User not a member of event team");
        }
        return ea;
    }

    public EventAttendee setEvent(long eventId) {
        EventAttendee result = getEventAttendee(eventId);
        eventAttendeeCache.setEventAttendee(result);
        return result;
    }

    public boolean isPageAccessible(long pageId) {
        if (RequestContextHolder.getRequestAttributes() == null)
            return false;
        var ea = eventAttendeeCache.getEventAttendee();
        if (ea == null)
            return false;

        LocalDateTime now = LocalDateTime.now();
        boolean isCurrent = ea.getEvent().getStartDate().isBefore(now) && ea.getEvent().getEndDate().isAfter(now);
        if (!isCurrent)
            return false;
        EventTeamPage etp = eventTeamPageRepository.findByEventTeamIdAndPageId(ea.getTeam().getId(), pageId);
        return etp != null;
    }

    public Event getCurrentEvent() {
        if (RequestContextHolder.getRequestAttributes() == null)
            return null;
        var ea = eventAttendeeCache.getEventAttendee();
        if (ea == null)
            return null;
        return ea.getEvent();
    }

    @Transactional(readOnly = true)
    public Page getPageWithBlocks(long eventId, long pageId) {
        EventAttendee ea = setEvent(eventId);
        EventTeamPage etp = eventTeamPageRepository.findByEventTeamIdAndPageId(ea.getTeam().getId(), pageId);
        if (etp == null) {
            throw new EntityNotFoundException("Page not found");
        }
        Page page = etp.getPage();
        page.setViewMode(ViewMode.TRANSCRIBE);
        page.setTranscriptionIdForEvent(page.getTranscription().getId());

        page.setTitleLabel(ea.getEvent().getName() + " | " + ea.getTeam().getName());
        List<Page> pages = eventTeamPageRepository.findPagesByEventTeamId(ea.getTeam().getId());
        int pageIndex = pages.indexOf(page);
        page.setPageNo(pageIndex + 1);
        if (pageIndex > 0) {
            page.setPrevPageId(pages.get(pageIndex - 1).getId());
        }
        if (pageIndex < pages.size() - 1) {
            page.setNextPageId(pages.get(pageIndex + 1).getId());
        }
        var verseService = BeanLoader.getBean(VerseService.class);
        var blockService = BeanLoader.getBean(BlockService.class);
        List<Verse> verses = verseService.completeVerses(page);
        page.setVerses(verses);

        for (PageLayer pageLayer : page.getPageLayers()) {
            List<Object> blocks = blockService.getBlocks(pageLayer, verses);
            pageLayer.setBlocks(blocks);
        }
        return page;
    }

    @Transactional(readOnly = true)
    public List<PageIdAndPageNo> getPagesIdsAndPageNums(long eventId) {
        EventAttendee ea = setEvent(eventId);
        List<Page> pages = eventTeamPageRepository.findPagesByEventTeamId(ea.getTeam().getId());
        int[] pageNo = { 1 };
        return pages.stream().map(p -> new PageIdAndPageNoImpl(p.getId(), pageNo[0]++)).collect(toList());
    }

    @Data
    @AllArgsConstructor
    public static class PageIdAndPageNoImpl implements PageIdAndPageNo {
        private Long id;
        private Integer pageNo;
    }

    @Transactional(readOnly = true)
    public String infoJsonProvider(long eventId, long pageId) {
        EventAttendee ea = setEvent(eventId);
        EventTeamPage etp = eventTeamPageRepository.findByEventTeamIdAndPageId(ea.getTeam().getId(), pageId);
        if (etp == null) {
            throw new EntityNotFoundException("Page not found");
        }
        Page page = etp.getPage();
        return iiifServerRepository.iiifServerInfoJsonProvider(iiifUriResolver.getInternalInfoJsonUri(page));
    }

    @SneakyThrows
    public String getSearchResults(long eventId, String q) {
        String origin = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        EventAttendee ea = setEvent(eventId);
        List<Page> pages = eventTeamPageRepository.findPagesByEventTeamId(ea.getTeam().getId());
        List<PageLayer> layers = pages.stream().map(p -> p.getPageLayers().get(0)).collect(toList());

        List<Verse> verses = verseRepository.findAllByPageLayersAndQ(layers, q);

        String resultId = URI.create(origin + "/api/events/" + eventId + "/iiif/search?q=" + q).toString();
        SearchResult searchResult = new SearchResult(resultId);
        var iiifPresentation2Service = BeanLoader.getBean(IIIFPresentation2Service.class);
        for (Verse verse : verses) {
            Long transcriptionId = verse.getPage().getTranscription().getId();
            String canvasId =
                    iiifUriResolver.getComponentUrl(origin, transcriptionId, "canvas", verse.getPage().getId());
            Annotation annotation = iiifPresentation2Service.createAnnotation(transcriptionId, verse, canvasId);

            SearchHit searchHit = new SearchHit();
            searchHit.addAnnotation(new Annotation(annotation.getIdentifier().toString()));
            searchResult.addHit(searchHit);
            searchResult.addResource(annotation);
        }

        return new IiifObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(searchResult);
    }

    @Component
    @SessionScope
    @Data
    private class EventAttendeeCache {
        private EventAttendee eventAttendee;
    }
}
