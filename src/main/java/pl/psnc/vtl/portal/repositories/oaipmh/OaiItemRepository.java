package pl.psnc.vtl.portal.repositories.oaipmh;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.time.DateUtils;
import org.dspace.xoai.dataprovider.filter.ScopedFilter;
import org.dspace.xoai.dataprovider.handlers.results.ListItemIdentifiersResult;
import org.dspace.xoai.dataprovider.handlers.results.ListItemsResults;
import org.dspace.xoai.dataprovider.model.Item;
import org.dspace.xoai.dataprovider.model.ItemIdentifier;
import org.dspace.xoai.dataprovider.repository.ItemRepository;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.transcriptions.PaginatedTranscriptions;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionListService;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionService;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionStatisticsService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OaiItemRepository implements ItemRepository {

    private final TranscriptionListService transcriptionListService;
    private final TranscriptionStatisticsService transcriptionStatisticsService;
    private final TranscriptionService transcriptionService;

    public Optional<Date> getMinDate() {
        return transcriptionStatisticsService.getMinLastUpdatedPublicTranscription();
    }

    @Override
    public Item getItem(String identifier) {

        Transcription transcription = transcriptionService.requireTranscription(
                OaiItemUtils.getTranscriptionIdFromOaiId(identifier));
        transcriptionService.completeTranscription(transcription, false, 1, 1);
        return new OaiItem(transcription);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length) {
        int page = Math.floorDiv(offset, length);
        PaginatedTranscriptions<Transcription> transcriptionsPage = transcriptionListService.getPublicTranscriptions(
                page, length, null, null);
        return pageToListItemIdentifiersResult(transcriptionsPage);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, Date from) {
        int page = Math.floorDiv(offset, length);
        PaginatedTranscriptions<Transcription> transcriptionsPage = transcriptionListService.getPublicTranscriptions(
                page, length, DateUtils.truncate(from, Calendar.DATE), null);
        return pageToListItemIdentifiersResult(transcriptionsPage);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiersUntil(List<ScopedFilter> filters, int offset, int length,
            Date until) {
        int page = Math.floorDiv(offset, length);
        PaginatedTranscriptions<Transcription> transcriptionsPage = transcriptionListService.getPublicTranscriptions(
                page, length, null, DateUtils.truncate(DateUtils.addDays(until, 1), Calendar.DATE));
        return pageToListItemIdentifiersResult(transcriptionsPage);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, Date from,
            Date until) {
        int page = Math.floorDiv(offset, length);
        PaginatedTranscriptions<Transcription> transcriptionsPage = transcriptionListService.getPublicTranscriptions(
                page, length, DateUtils.truncate(from, Calendar.DATE), DateUtils.truncate(DateUtils.addDays(until, 1), Calendar.DATE));
        return pageToListItemIdentifiersResult(transcriptionsPage);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length,
            String setSpec) {
        return getItemIdentifiers(filters, offset, length);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length,
            String setSpec, Date from) {
        return getItemIdentifiers(filters, offset, length, from);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiersUntil(List<ScopedFilter> filters, int offset, int length,
            String setSpec, Date until) {
        return getItemIdentifiersUntil(filters, offset, length, until);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length,
            String setSpec, Date from, Date until) {
        return getItemIdentifiers(filters, offset, length, from, until);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length) {
        int page = Math.floorDiv(offset, length);
        PaginatedTranscriptions<Transcription> transcriptionsPage = transcriptionListService.getPublicTranscriptions(
                page, length, null, null);
        return pageToListItemResults(transcriptionsPage);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, Date from) {
        int page = Math.floorDiv(offset, length);
        PaginatedTranscriptions<Transcription> transcriptionsPage = transcriptionListService.getPublicTranscriptions(
                page, length, DateUtils.truncate(from, Calendar.DATE), null);
        return pageToListItemResults(transcriptionsPage);
    }

    @Override
    public ListItemsResults getItemsUntil(List<ScopedFilter> filters, int offset, int length, Date until) {
        int page = Math.floorDiv(offset, length);
        PaginatedTranscriptions<Transcription> transcriptionsPage = transcriptionListService.getPublicTranscriptions(
                page, length, null, DateUtils.truncate(DateUtils.addDays(until, 1), Calendar.DATE));
        return pageToListItemResults(transcriptionsPage);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, Date from, Date until) {
        int page = Math.floorDiv(offset, length);
        PaginatedTranscriptions<Transcription> transcriptionsPage = transcriptionListService.getPublicTranscriptions(
                page, length, DateUtils.truncate(from, Calendar.DATE), DateUtils.truncate(DateUtils.addDays(until, 1), Calendar.DATE));
        return pageToListItemResults(transcriptionsPage);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, String setSpec) {
        return getItems(filters, offset, length);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, String setSpec, Date from) {
        return getItems(filters, offset, length, from);
    }

    @Override
    public ListItemsResults getItemsUntil(List<ScopedFilter> filters, int offset, int length, String setSpec,
            Date until) {
        return getItemsUntil(filters, offset, length, until);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, String setSpec, Date from,
            Date until) {
        return getItems(filters, offset, length, from, until);
    }

    private ListItemIdentifiersResult pageToListItemIdentifiersResult(PaginatedTranscriptions<Transcription> page) {
        List<ItemIdentifier> itemIdentifiers = page.getTranscriptions().stream()
                .map(OaiItemIdentifier::fromTranscription).collect(Collectors.toList());
        return new ListItemIdentifiersResult(page.getPage() < (page.getPages() - 1), itemIdentifiers, (int) page.getHits());
    }

    private ListItemsResults pageToListItemResults(PaginatedTranscriptions<Transcription> page) {
        List<Item> oaiItems = page.getTranscriptions().stream().map(OaiItem::new).collect(Collectors.toList());
        return new ListItemsResults(page.getPage() < (page.getPages() - 1), oaiItems, (int) page.getHits());
    }
}
