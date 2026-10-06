package pl.psnc.vtl.portal.services.transcriptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.transaction.support.TransactionTemplate;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionContinuation;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionPermissions;
import pl.psnc.vtl.portal.model.permissions.TranscriptionGroup;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUser;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.users.Group;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.users.UserRole;
import pl.psnc.vtl.portal.repositories.iiiftools.DeleteTranscriptionImagesEmailService;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsRepository;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.*;
import pl.psnc.vtl.portal.repositories.users.UserGroupRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.security.CustomUser;
import pl.psnc.vtl.portal.services.indexes.IndexService;
import pl.psnc.vtl.portal.services.layers.LayerService;
import pl.psnc.vtl.portal.services.pages.ImportPageRecognizerFactory;
import pl.psnc.vtl.portal.services.pages.PageImportService;
import pl.psnc.vtl.portal.services.pages.PageListService;
import pl.psnc.vtl.portal.services.pages.PageService;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.progress.ProgressService;
import pl.psnc.vtl.portal.services.properties.PropertyService;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;
import pl.psnc.vtl.portal.services.users.UserService;
import pl.psnc.vtl.portal.services.verses.UndoRedoService;

import javax.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TranscriptionServiceTest {

    @Mock
    private TranscriptionRepository transcriptionRepository;
    @Mock
    private TranscriptionInfoRepository transcriptionInfoRepository;

    @Mock
    private TranscriptionPermissionService transcriptionPermissionService;

    @Mock
    private TranscriptionValidator transcriptionValidator;
    @Mock
    private TranscriptionMetadataValidator transcriptionMetadataValidator;

    @Mock
    private TranscriptionInfoValidator transcriptionInfoValidator;

    @Mock
    private PageService pageService;

    @Mock
    private PageImportService pageImportService;

    @Mock
    private ImportPageRecognizerFactory importPageRecognizerFactory;

    @Mock
    private PageListService pageListService;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private VerseRepository verseRepository;

    @Mock
    private TranscriptionUserRepository transcriptionUserRepository;

    @Mock
    private TranscriptionGroupRepository transcriptionGroupRepository;

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private IIIFToolsRepository iiifToolsRepository;

    @Mock
    private PropertyService propertyService;

    @Mock
    private DeleteTranscriptionImagesEmailService deleteTranscriptionImagesEmailService;

    @Mock
    private TranscriptionUserLastPageRepository transcriptionUserLastPageRepository;

    @Mock
    private UserLastTranscriptionRepository userLastTranscriptionRepository;

    @Mock
    private TranscriptionContinuationService transcriptionContinuationService;

    @Mock
    private TranscriptionProcessingCoordinator transcriptionProcessingCoordinator;

    @Mock
    private ModificationTrackingService modificationTrackingService;

    @Mock
    private ProgressService progressService;

    @Mock
    private UndoRedoService undoRedoService;

    @Mock
    private UserService userService;

    @Mock
    private IndexService indexService;

    @Mock
    private LayerService layerService;

    @Mock
    private LoggedInUserHolder loggedInUserHolder;

    @Mock
    private TransactionTemplate transactionTemplate;

    TranscriptionService transcriptionService;

    @Before
    public void resetMocks() {
        Mockito.reset(transcriptionRepository);
        Mockito.reset(transcriptionGroupRepository);
        Mockito.reset(transcriptionUserRepository);

        transcriptionService = new TranscriptionService(
                transcriptionRepository,
                transcriptionInfoRepository,
                transcriptionPermissionService,
                transcriptionValidator,
                transcriptionMetadataValidator,
                transcriptionInfoValidator,
                pageService,
                pageImportService,
                importPageRecognizerFactory,
                pageListService,
                pageRepository,
                verseRepository,
                transcriptionUserRepository,
                transcriptionGroupRepository,
                userGroupRepository,
                iiifToolsRepository,
                propertyService,
                deleteTranscriptionImagesEmailService,
                transcriptionUserLastPageRepository,
                userLastTranscriptionRepository,
                transcriptionContinuationService,
                transcriptionProcessingCoordinator,
                modificationTrackingService,
                progressService,
                undoRedoService,
                userService,
                indexService,
                layerService,
                loggedInUserHolder,
                transactionTemplate);

        CustomUser user = new CustomUser(1L, "username@wp.pl", "passwd", true, false, new ArrayList<>());

        SecurityContext context = new SecurityContextImpl();
        Authentication authentication = new RememberMeAuthenticationToken("key", user, Collections.emptyList());

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void shouldPrepareTranscriptionPermissions() {
        //given
        TranscriptionContinuation transcriptionContinuation = TranscriptionContinuation
                .builder()
                .lastPageId(1L)
                .state(TranscriptionContinuation.State.IN_PROGRESS)
                .build();

        Transcription transcription = Transcription
                .builder()
                .hidden(true)
                .continuation(transcriptionContinuation)
                .build();

        TranscriptionGroup transcriptionGroup = TranscriptionGroup
                .builder()
                .transcription(transcription)
                .group(Group
                        .builder()
                        .name("sampleGroup")
                        .build())
                .build();

        TranscriptionUser transcriptionUser = TranscriptionUser
                .builder()
                .user(User.builder().email("sampleEmail.com").build())
                .role(UserRole.builder().code("sampleRoleCode").build()).build();

        Mockito.when(transcriptionRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(transcription));
        Mockito.when(transcriptionGroupRepository.findByTranscriptionId(Mockito.anyLong()))
                .thenReturn(Arrays.asList(transcriptionGroup));
        Mockito.when(transcriptionUserRepository.findByTranscriptionId(Mockito.anyLong()))
                .thenReturn(Arrays.asList(transcriptionUser));
        Mockito.when(transcriptionContinuationService.constructContinuation(Mockito.any()))
                .thenReturn(transcriptionContinuation);
        //when
        TranscriptionPermissions transcriptionPermissions = transcriptionService.getTranscriptionPermissions(1);
        //then
        assertThat(transcriptionPermissions.getAllowedGroups().size(), is(1));
        assertThat(transcriptionPermissions.getAllowedUsers().size(), is(1));
        assertThat(transcriptionPermissions.getAllowedGroups().get(0).getGroup().getName(), is("sampleGroup"));
        assertThat(transcriptionPermissions.getAllowedUsers().get(0).getUser().getEmail(), is("sampleEmail.com"));
        assertThat(transcriptionPermissions.getAllowedUsers().get(0).getRole().getCode(), is("sampleRoleCode"));
        assertThat(transcriptionPermissions.getTranscriptionContinuation(), is(transcriptionContinuation));
    }


    @Test
    public void shouldPrepareTranscriptionWithoutUsersAndGroupsPermissions() {
        //given
        Transcription transcription = Transcription
                .builder()
                .hidden(true)
                .build();

        Mockito.when(transcriptionRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(transcription));
        Mockito.when(transcriptionGroupRepository.findByTranscriptionId(Mockito.anyLong()))
                .thenReturn(Collections.emptyList());
        Mockito.when(transcriptionUserRepository.findByTranscriptionId(Mockito.anyLong()))
                .thenReturn(Collections.emptyList());
        //when
        TranscriptionPermissions transcriptionPermissions = transcriptionService.getTranscriptionPermissions(1);
        //then
        assertThat(transcriptionPermissions.getAllowedGroups().size(), is(0));
        assertThat(transcriptionPermissions.getAllowedUsers().size(), is(0));
    }


    @Test(expected = EntityNotFoundException.class)
    public void shouldPrepareEmptyTranscriptionPermissions() {
        //given
        Mockito.when(transcriptionRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());
        //when
        TranscriptionPermissions transcriptionPermissions = transcriptionService.getTranscriptionPermissions(1);
        //then exception is expected as declared in @Test annotation

    }


    @Test
    public void shouldMakeNonHiddenTranscriptionHidden() {
        //given
        Transcription transcription = Transcription
                .builder()
                .hidden(false)
                .build();

        Mockito.when(transcriptionRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(transcription));

        //when
        Transcription resultingTranscription = transcriptionService.changeTranscriptionVisibility(1, true);

        //then
        assertThat(resultingTranscription.isHidden(), is(true));
    }

    @Test
    public void shouldSetTranscriptionDatePublishedWhenChangingToNotHidden() {
        //given
        Transcription transcription = Transcription
                .builder()
                .hidden(true)
                .build();

        Mockito.when(transcriptionRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(transcription));

        //when
        Transcription resultingTranscription = transcriptionService.changeTranscriptionVisibility(1, false);

        //then
        assertNotNull(resultingTranscription.getDatePublished());
    }

    @Test
    public void shouldNullTranscriptionDatePublishedWhenChangingToHidden() {
        //given
        Transcription transcription = Transcription
                .builder()
                .hidden(false)
                .datePublished(LocalDateTime.now())
                .build();

        Mockito.when(transcriptionRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(transcription));

        //when
        Transcription resultingTranscription = transcriptionService.changeTranscriptionVisibility(1, true);

        //then
        assertNull(resultingTranscription.getDatePublished());
    }


    @Test
    public void shouldMakeNonHiddenTranscriptionNotHidden() {
        //given
        Transcription transcription = Transcription
                .builder()
                .hidden(false)
                .build();

        Mockito.when(transcriptionRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(transcription));

        //when
        Transcription resultingTranscription = transcriptionService.changeTranscriptionVisibility(1, false);

        //then
        assertThat(resultingTranscription.isHidden(), is(false));
    }


    @Test
    public void shouldMakeHiddenTranscriptionNotHidden() {
        //given
        Transcription transcription = Transcription
                .builder()
                .hidden(true)
                .build();

        Mockito.when(transcriptionRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(transcription));

        //when
        Transcription resultingTranscription = transcriptionService.changeTranscriptionVisibility(1, false);

        //then
        assertThat(resultingTranscription.isHidden(), is(false));
    }


    @Test
    public void shouldMakeHiddenTranscriptionHidden() {
        //given
        Transcription transcription = Transcription
                .builder()
                .hidden(true)
                .build();

        Mockito.when(transcriptionRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(transcription));

        //when
        Transcription resultingTranscription = transcriptionService.changeTranscriptionVisibility(1, true);

        //then
        assertThat(resultingTranscription.isHidden(), is(true));
    }
}