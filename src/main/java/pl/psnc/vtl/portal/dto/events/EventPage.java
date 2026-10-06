package pl.psnc.vtl.portal.dto.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.psnc.vtl.portal.model.pages.Page;

@Getter
@Setter
@RequiredArgsConstructor
public class EventPage extends Page {
    private final EventTeamCore eventTeam;

    public EventPage(Page page, EventTeamCore eventTeam) {
        setId(page.getId());
        setTranscription(page.getTranscription());
        setPageLayers(page.getPageLayers());
        setPath(page.getPath());
        setPageNo(page.getPageNo());
        setRelativeNo(page.getRelativeNo());
        setBoundingBox(page.getBoundingBox());
        setIiifUrl(page.getIiifUrl());
        setThumbnailUrl(page.getThumbnailUrl());
        setVerses(page.getVerses());
        setStudentVerses(page.getStudentVerses());
        setTitleLabel(page.getTitleLabel());
        setPrevPageId(page.getPrevPageId());
        setNextPageId(page.getNextPageId());
        setViewMode(page.getViewMode());
        setPercentOfTranscribed(page.getPercentOfTranscribed());
        setPercentOfVerified(page.getPercentOfVerified());
        setEmpty(page.isEmpty());
        setLastModifier(page.getLastModifier());
        setLastModificationDate(page.getLastModificationDate());
        setTeiPbValue(page.getTeiPbValue());
        this.eventTeam = eventTeam;
    }
}