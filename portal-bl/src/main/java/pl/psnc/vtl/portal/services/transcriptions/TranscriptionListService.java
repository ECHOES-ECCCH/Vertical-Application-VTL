package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.dto.transcriptions.*;
import pl.psnc.vtl.portal.dto.transcriptions.list.AnalyticInfoDto;
import pl.psnc.vtl.portal.dto.transcriptions.list.TranscriptionInfoRegularDto;
import pl.psnc.vtl.portal.dto.transcriptions.list.TranscriptionInfoUsersDto;
import pl.psnc.vtl.portal.dto.transcriptions.list.UserInfoDto;
import pl.psnc.vtl.portal.model.permissions.TranscriptionGroup;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUser;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.transcriptions.*;
import pl.psnc.vtl.portal.repositories.users.UserGroupRepository;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.services.pages.PageListService;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class TranscriptionListService {

    private final TranscriptionService transcriptionService;

    private final TranscriptionPermissionService transcriptionPermissionService;

    private final PageListService pageListService;

    private final TranscriptionRepository transcriptionRepository;

    private final TranscriptionInfoRepository transcriptionInfoRepository;

    private final AnalyticInfoRepository analyticInfoRepository;

    private final MonographicInfoRepository monographicInfoRepository;

    private final TranscriptionUserRepository transcriptionUserRepository;

    private final TranscriptionGroupRepository transcriptionGroupRepository;

    private final UserGroupRepository userGroupRepository;

    private final UserRepository userRepository;

    private final LoggedInUserHolder loggedInUserHolder;

    public PaginatedTranscriptions<TranscriptionSearchResult> searchTranscriptions(String q, SearchType type, User user,
            SearchScope scope, SearchVisibility visibility, SearchOrder order, int page, int perpage) {
        if (user == null) {
            user = loggedInUserHolder.getLoggedInUser();
        }

        if (type == SearchType.METADATA && order == SearchOrder.HIT_COUNT) {
            order = SearchOrder.LAST_MODIFICATION_DATE;
        }

        return getTranscriptions(q, type, loggedInUserHolder.getLoggedInUser(), scope, visibility, order, null, page,
                perpage,
                data -> {
                    prepareTranscriptionsList(data.stream().map(t -> t.getTranscription()).collect(toList()));
                    List<TranscriptionSearchResult> results = data.stream()
                            .map(t -> new TranscriptionSearchResult(t.getTranscription())).collect(toList());
                    if (!StringUtils.isBlank(q)) {
                        if (type == SearchType.METADATA || type == SearchType.ALL) {
                            results.forEach(r -> r.checkMetadata(q));
                        }
                        if (type == SearchType.CONTENT || type == SearchType.ALL) {
                            List<Long> resIds =
                                    results.stream().map(t -> t.getTranscription().getId()).collect(toList());
                            Map<Long, TranscriptionSearchResult> resById =
                                    results.stream().collect(toMap(t -> t.getTranscription().getId(), t -> t));
                            pageListService.searchPages(q, resIds)
                                    .forEach(p -> resById.get(p.getPage().getTranscription().getId()).addPage(p));
                        }
                    }
                    return results;
                });
    }

    public PaginatedTranscriptions<Transcription> getTranscriptions(String q, SearchType type,
            SearchScope scope, SearchVisibility visibility, SearchOrder order, Sort.Direction orderDir, int page,
            int perpage) {
        return getTranscriptions(q, type, loggedInUserHolder.getLoggedInUser(), scope, visibility, order, orderDir,
                page, perpage, data -> {
                    List<Transcription> transcriptions = data.stream().map(t -> t.getTranscription()).collect(toList());
                    prepareTranscriptionsList(transcriptions);
                    return transcriptions;
                });
    }

    private void prepareTranscriptionsList(List<Transcription> transcriptions) {
        pageListService.fillPages(transcriptions, 1, 6);

        analyticInfoRepository.findWithActorsByIdIn(
                transcriptions.stream().map(t -> t.getAnalyticInfo().getId()).collect(toList()));
        monographicInfoRepository.findWithActorsByIdIn(
                transcriptions.stream().map(t -> t.getMonographicInfo().getId()).collect(toList()));
        transcriptionInfoRepository.findByIdIn(
                transcriptions.stream().map(t -> t.getTranscriptionInfo().getId()).collect(toList()));

        for (Iterator<Transcription> it = transcriptions.iterator(); it.hasNext();) {
            Transcription t = it.next();
            try {
                transcriptionService.completeTranscription(t, false, 0, 0);
            } catch (AccessDeniedException e) {
                it.remove();
                continue;
            }
            t.setProfileDesc(null);
            t.setCreator(null);
            t.getPages().getList()
                    .forEach(p -> p.getPageLayers()
                            .forEach(pl -> pl.setVerses(null)));
        }
    }

    public PaginatedTranscriptions<Transcription> getPublicTranscriptions(int page, int perpage, @Nullable Date from, @Nullable Date until) {
        Page<Transcription> transcriptionsPage = getTranscriptionsPage(page, perpage, from, until);
        return PaginatedTranscriptions.<Transcription>builder().page(page).perpage(perpage).hits(transcriptionsPage.getTotalElements())
                .count(transcriptionsPage.getNumberOfElements()).pages(totalPages(transcriptionsPage.getTotalElements(), perpage))
                .transcriptions(transcriptionsPage.getContent()).build();
    }

    private Page<Transcription> getTranscriptionsPage(int page, int perpage, Date from, Date until) {
        if (Objects.nonNull(from) && Objects.nonNull(until)) {
            return transcriptionRepository.findAllByHiddenAndLastModificationDateGreaterThanEqualAndLastModificationDateLessThanEqualOrderByLastModificationDateAsc(
                    false, from.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime(), until.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime(),
                    PageRequest.of(page, perpage));
        } else if (Objects.nonNull(from)) {
            return transcriptionRepository.findAllByHiddenAndLastModificationDateGreaterThanEqualOrderByLastModificationDateAsc(
                    false, from.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime(),
                    PageRequest.of(page, perpage));
        } else if (Objects.nonNull(until)) {
            return transcriptionRepository.findAllByHiddenAndLastModificationDateLessThanEqualOrderByLastModificationDateAsc(
                    false, until.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime(), PageRequest.of(page, perpage));
        } else {
            return transcriptionRepository.findAllByHiddenOrderByLastModificationDateAsc(
                    false, PageRequest.of(page, perpage));
        }
    }

    public PaginatedTranscriptions<TranscriptionInfoRegularDto> getTranscriptionsInfo(String q, SearchType type,
            SearchScope scope, SearchVisibility visibility, SearchOrder order, Sort.Direction orderDir, int page,
            int perpage) {
        return getTranscriptions(q, type, loggedInUserHolder.getLoggedInUser(), scope, visibility, order, orderDir,
                page, perpage, data -> {
                    List<Long> ids = data.stream().map(t -> t.getTranscription().getId()).collect(toList());
                    ids.forEach(transcriptionPermissionService::checkReadAccess);
                    return transcriptionRepository.findTranscriptionInfoRegularByIds(ids).stream()
                            .map(t -> t.toTranscriptionInfoDto()).collect(toList());
                });
    }

    private <T> PaginatedTranscriptions<T> getTranscriptions(String q, SearchType type, User user, SearchScope scope,
            SearchVisibility visibility, SearchOrder order, Sort.Direction orderDir, int page, int perpage,
            Function<Page<TranscriptionWithHitCount>, List<T>> retrieveResultsFunction) {

        if (type == SearchType.METADATA && order == SearchOrder.HIT_COUNT) {
            order = SearchOrder.LAST_MODIFICATION_DATE;
        }

        Page<TranscriptionWithHitCount> data = transcriptionRepository.searchTranscriptions(q, type, user, scope,
                visibility, order, orderDir, page, perpage);

        List<T> results = retrieveResultsFunction.apply(data);

        return PaginatedTranscriptions.<T>builder().page(page).perpage(perpage).hits(data.getTotalElements())
                .count(data.getNumberOfElements()).pages(totalPages(data.getTotalElements(), perpage))
                .transcriptions(results).build();
    }

    public PaginatedTranscriptionsWithOwner<Transcription> getUserTranscriptions(Long ownerId, int page, int perpage,
            String q, SearchOrder order, Sort.Direction orderDir) {
        return getUserTranscriptions(ownerId, page, perpage, q, order, orderDir,
                data -> {
                    List<Transcription> transcriptions = data.stream().map(t -> t.getTranscription()).collect(toList());
                    prepareTranscriptionsList(transcriptions);
                    return transcriptions;
                });
    }

    public PaginatedTranscriptionsWithOwner<TranscriptionInfoUsersDto> getUserTranscriptionsInfo(Long ownerId,
            int page, int perpage, String q, SearchOrder order, Sort.Direction orderDir) {
        return getUserTranscriptions(ownerId, page, perpage, q, order, orderDir,
                data -> {
                    List<Long> transcriptionIds =
                            data.stream().map(t -> t.getTranscription().getId()).collect(toList());
                    transcriptionIds.forEach(transcriptionPermissionService::checkReadAccess);

                    analyticInfoRepository.findWithActorsByIdIn(
                            data.get().map(t -> t.getTranscription().getAnalyticInfo().getId()).collect(toList()));

                    Map<Long, List<TranscriptionUser>> users = transcriptionUserRepository
                            .findByTranscriptionIdIn(transcriptionIds).stream()
                            .collect(Collectors.groupingBy(tu -> tu.getTranscription().getId()));
                    Map<Long, List<TranscriptionGroup>> groups = transcriptionGroupRepository
                            .findByTranscriptionIdIn(transcriptionIds).stream()
                            .collect(Collectors.groupingBy(tg -> tg.getTranscription().getId()));
                    return data.stream().map(twhc -> {
                        Transcription t = twhc.getTranscription();
                        Set<User> tUsers = new LinkedHashSet<>();
                        users.getOrDefault(t.getId(), List.of()).stream()
                                .map(TranscriptionUser::getUser).forEach(tUsers::add);
                        List<Long> groupIds = groups.getOrDefault(t.getId(), List.of()).stream()
                                .map(tg -> tg.getGroup().getId()).collect(toList());
                        userGroupRepository.findByGroupIdIn(groupIds).forEach(ug -> tUsers.add(ug.getUser()));

                        return TranscriptionInfoUsersDto.infoUsersDtoBuilder().id(t.getId())
                                .analyticInfo(new AnalyticInfoDto(t.getAnalyticInfo().getTitle()))
                                .ownerEmail(t.getCreator().getEmail())
                                .users(tUsers.stream().map(u -> new UserInfoDto(u.getId(), u.getEmail()))
                                        .collect(toList()))
                                .build();
                    }).collect(toList());
                });
    }

    private <T> PaginatedTranscriptionsWithOwner<T> getUserTranscriptions(Long ownerId, int page, int perpage, String q,
            SearchOrder order, Sort.Direction orderDir,
            Function<Page<TranscriptionWithHitCount>, List<T>> retrieveResultsFunction) {
        User owner = userRepository.findById(ownerId).orElseThrow(() -> new EntityNotFoundException(
                "Unable to find " + User.class.getName() + " with id '" + ownerId + "'!"));

        Page<TranscriptionWithHitCount> data = transcriptionRepository.searchTranscriptions(q, SearchType.ALL, owner,
                SearchScope.CREATOR, SearchVisibility.PUBLIC, order, orderDir, page, perpage);

        List<T> results = retrieveResultsFunction.apply(data);

        return PaginatedTranscriptionsWithOwner.<T>builder().page(page).perpage(perpage).hits(data.getTotalElements())
                .count(data.getNumberOfElements()).pages(totalPages(data.getTotalElements(), perpage))
                .transcriptions(results).ownerEmail(owner.getEmail()).build();
    }

    private int totalPages(long totalElements, int perpage) {
        int pages = (int) (totalElements / perpage);
        if (totalElements % perpage > 0) {
            pages++;
        }
        return pages;
    }
}
