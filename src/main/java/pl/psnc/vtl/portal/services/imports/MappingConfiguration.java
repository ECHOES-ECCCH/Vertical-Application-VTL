package pl.psnc.vtl.portal.services.imports;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.transcriptions.ActorRoleId;
import pl.psnc.vtl.portal.repositories.properties.TranscriptionPropertiesRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.ActorRoleRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.LanguageRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MappingConfiguration {

    private final String ANALITIC_CHARACTERISTICS = "analytic-characteristics";
    private final String LANGUAGE_CODES = "language-codes";
    private final TranscriptionPropertiesRepository mappingPropertiesRepository;
    private final LanguageRepository languageRepository;
    private  final ActorRoleRepository actorRoleRepository;

    private Map<String, String> languageCodes;

    private Map<String, ActorRoleId> actorRoles;

    private void initializeLanguageCodes() {
        languageCodes = new HashMap<>();
        languageRepository.findAll().forEach(lang ->
        mappingPropertiesRepository.getProperties(LANGUAGE_CODES + "." + lang.getCode())
                .forEach(code -> languageCodes.put(code, lang.getCode())));
    }

    private void initializeActorRoles() {
        actorRoles = new HashMap<>();
        actorRoleRepository.findAll().forEach(role -> actorRoles.put(role.getCode(), new ActorRoleId(role.getCode())));
    }



    public String getLanguageCode(String language) {
        if(languageCodes == null) initializeLanguageCodes();
        return languageCodes.get(language);
    }

    public Set<String> getAnalyticCharacteristics() {
        return new HashSet<>(mappingPropertiesRepository.getProperties(ANALITIC_CHARACTERISTICS));
    }

    public ActorRoleId getActorRoleId(String roleCode){
        if (actorRoles == null) initializeActorRoles();
        return actorRoles.getOrDefault(roleCode, null);
    }
}
