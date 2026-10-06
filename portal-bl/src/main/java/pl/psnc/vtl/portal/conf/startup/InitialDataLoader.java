package pl.psnc.vtl.portal.conf.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.model.events.Event;
import pl.psnc.vtl.portal.model.indexes.Index;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.permissions.TranscriptionGroup;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUser;
import pl.psnc.vtl.portal.model.transcriptions.ActorRole;
import pl.psnc.vtl.portal.model.transcriptions.BiblType;
import pl.psnc.vtl.portal.model.transcriptions.Language;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.users.Group;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.users.UserGroup;
import pl.psnc.vtl.portal.model.users.UserRole;
import pl.psnc.vtl.portal.model.verses.Comment;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.verses.VerseContent;
import pl.psnc.vtl.portal.repositories.events.EventRepository;
import pl.psnc.vtl.portal.repositories.indexes.IndexRepository;
import pl.psnc.vtl.portal.repositories.layers.LayerRepository;
import pl.psnc.vtl.portal.repositories.pages.PageLayersRepository;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.*;
import pl.psnc.vtl.portal.repositories.users.GroupRepository;
import pl.psnc.vtl.portal.repositories.users.UserGroupRepository;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.repositories.users.UserRoleRepository;
import pl.psnc.vtl.portal.repositories.verses.CommentRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseContentRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;

import javax.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InitialDataLoader {

    private final UserRoleRepository userRoleRepository;

    private final LanguageRepository languageRepository;

    private final BiblTypeRepository biblTypeRepository;

    private final ActorRoleRepository actorRoleRepository;

    private final UserRepository userRepository;

    private final EventRepository eventRepository;

    private final PasswordEncoder passwordEncoder;

    private final GroupRepository groupRepository;

    private final UserGroupRepository userGroupRepository;

    private final TranscriptionRepository transcriptionRepository;

    private final TranscriptionUserRepository transcriptionUserRepository;

    private final TranscriptionGroupRepository transcriptionGroupRepository;

    private final PageRepository pageRepository;
    private final PageLayersRepository pageLayersRepository;
    private final LayerRepository layerRepository;

    private final VerseRepository verseRepository;

    private final IndexRepository indexRepository;

    private final CommentRepository commentRepository;

    private final VerseContentRepository verseContentRepository;


    public void loadBasicData() {
        log.debug("Loading basic data");
        List<Map<String, List<Object>>> data = YamlLoader.loadYamlData("initial-data/basic-data.yml");

        if (userRoleRepository.count() == 0) {
            List<UserRole> userRoles = YamlLoader.getObjects(data, "UserRoles");
            userRoleRepository.saveAll(userRoles);
            log.debug("Loaded " + userRoles.size() + " 'UserRole' objects");
        } else {
            log.debug("'UserRole' objects are already loaded");
        }

        if (languageRepository.count() == 0) {
            List<Language> languages = YamlLoader.getObjects(data, "Languages");
            languageRepository.saveAll(languages);
            log.debug("Loaded " + languages.size() + " 'Language' objects");
        } else {
            log.debug("'Language' objects are already loaded");
        }

        if (biblTypeRepository.count() == 0) {
            List<BiblType> biblTypes = YamlLoader.getObjects(data, "BiblTypes");
            biblTypeRepository.saveAll(biblTypes);
            log.debug("Loaded " + biblTypes.size() + " 'BiblType' objects");
        } else {
            log.debug("'BiblType' objects are already loaded");
        }

        if (actorRoleRepository.count() == 0) {
            List<ActorRole> actorRoles = YamlLoader.getObjects(data, "ActorRoles");
            actorRoleRepository.saveAll(actorRoles);
            log.debug("Loaded " + actorRoles.size() + " 'ActorRole' objects");
        } else {
            log.debug("'ActorRole' objects are already loaded");
        }

    }


    public void loadProfileData(String... profiles) {
        log.debug("Loading " + Arrays.asList(profiles) + " data");

        List<Map<String, List<Object>>> data = YamlLoader.loadYamlData(Arrays.asList(profiles).stream()
                .map(profile -> "initial-data/profile/" + profile + "-data.yml").collect(Collectors.toList()));

        if (userRepository.count() == 0) {
            List<User> users = YamlLoader.getObjects(data, "Users");
            for (User user : users) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.setAcceptedRegulations(LocalDateTime.now());
            }
            userRepository.saveAll(users);
            log.debug("Loaded " + users.size() + " 'User' objects");
        } else {
            log.debug("'User' objects are already loaded");
        }

        List<Event> events = YamlLoader.getObjects(data, "Events");
        for(Event event: events){
            event.setStartDate(LocalDateTime.of(1990,1,1,10,10));
            event.setEndDate(LocalDateTime.of(2000,1,1,10,10));
        }
        if (eventRepository.count() == 0) {
            eventRepository.saveAll(events);
            log.debug("Loaded " + events.size() + " 'Event' objects");
        } else {
            log.debug("'Event' objects are already loaded");
        }

        if (groupRepository.count() == 0) {
            List<Group> groups = YamlLoader.getObjects(data, "Groups");
            groupRepository.saveAll(groups);
            for (Group group : groups) {
                UserGroup userGroup = new UserGroup();
                userGroup.setGroup(group);
                userGroup.setUser(group.getCreator());
                userGroup.setRole(userRoleRepository.getOne("admin"));
                userGroupRepository.save(userGroup);
            }
            log.debug("Loaded " + groups.size() + " 'Group' objects");

            List<UserGroup> userGroups = YamlLoader.getObjects(data, "UserGroups");
            userGroupRepository.saveAll(userGroups);
            log.debug("Loaded " + userGroups.size() + " 'UserGroup' objects");

        } else {
            log.debug("'Group' and 'UserGroup' objects are already loaded");
        }

        if (transcriptionRepository.count() == 0) {
            List<Transcription> transcriptions = YamlLoader.getObjects(data, "Transcriptions");
            transcriptions = transcriptionRepository.saveAll(transcriptions);
            for (Transcription transcription : transcriptions) {
                TranscriptionUser transcriptionUser = new TranscriptionUser();
                transcriptionUser.setTranscription(transcription);
                User user = userRepository.findById(transcription.getCreator().getId())
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
                transcriptionUser.setUser(user);
                transcriptionUser.setRole(userRoleRepository.getOne("admin"));
                transcriptionUserRepository.save(transcriptionUser);
                transcriptionRepository.save(transcription);
            }
            log.debug("Loaded " + transcriptions.size() + " 'Transcription' objects");

            List<TranscriptionUser> transcriptionUsers = YamlLoader.getObjects(data, "TranscriptionUsers");
            transcriptionUserRepository.saveAll(transcriptionUsers);
            log.debug("Loaded " + transcriptionUsers.size() + " 'TranscriptionUser' objects");

            List<TranscriptionGroup> transcriptionGroups = YamlLoader.getObjects(data, "TranscriptionGroups");
            transcriptionGroupRepository.saveAll(transcriptionGroups);
            log.debug("Loaded " + transcriptionGroups.size() + " 'TranscriptionGroup' objects");


        } else {
            log.debug("'Transcription', 'TranscriptionUser' and 'TranscriptionGroup' objects are already loaded");
        }

        if (pageRepository.count() == 0) {
            List<Layer> layers = YamlLoader.getObjects(data, "Layers");
            layerRepository.saveAll(layers);
            List<Page> pages = YamlLoader.getObjects(data, "Pages");
            pageRepository.saveAll(pages);
            log.debug("Loaded " + pages.size() + " 'Page' objects");
            List<PageLayer> pagesLayers = YamlLoader.getObjects(data, "PageLayers");
            pageLayersRepository.saveAll(pagesLayers);
            log.debug("Loaded " + pagesLayers.size() + " 'Pagelayers'  objects");

            List<Verse> verses = YamlLoader.getObjects(data, "Verses");
            verseRepository.saveAll(verses);
            log.debug("Loaded " + verses.size() + " 'Verse' objects");

            List<Index> indexes = YamlLoader.getObjects(data, "Indexes");
            indexRepository.saveAll(indexes);
            log.debug("Loaded " + indexes.size() + " 'Index' objects");

            List<Comment> comments = YamlLoader.getObjects(data, "Comments");
            commentRepository.saveAll(comments);
            log.debug("Loaded " + comments.size() + " 'Comment' objects");

        } else {
            log.debug("'Page', 'Verse' and 'Comment' objects are already loaded");
        }

        List<VerseContent> verseContents = YamlLoader.getObjects(data, "VerseContents");
        verseContentRepository.saveAll(verseContents);
    }


}
