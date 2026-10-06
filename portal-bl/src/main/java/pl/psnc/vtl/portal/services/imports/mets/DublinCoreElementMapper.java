package pl.psnc.vtl.portal.services.imports.mets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.transcriptions.ActorCore;
import pl.psnc.vtl.portal.dto.transcriptions.ActorRoleId;
import pl.psnc.vtl.portal.dto.transcriptions.LanguageId;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.MappingConfiguration;

import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
class DublinCoreElementMapper {

    private final MappingConfiguration dumpObject;

    @PostConstruct
    private void initMappingConfiguration(){
        configuration = dumpObject;
    }
    private static MappingConfiguration configuration;

    static TranscriptionCore setTitle(String a, TranscriptionCore c) {
        /*if(c.isAnalytic())*/ c.getAnalyticInfo().setTitle(a);
        if(!c.isAnalytic()) c.getMonographicInfo().setTitle(a);
        return c;
    }

    static TranscriptionCore setCreator(String a, TranscriptionCore c) {
        ActorCore actor = new ActorCore();
        actor.setName(a);
        actor.setRole(new ActorRoleId("author"));
        return setActor(actor, c);
    }

    static TranscriptionCore setRelation(String a, TranscriptionCore c) {
        if (c.isAnalytic()) c.getMonographicInfo().setTitle(a);
        return c;
    }

    static TranscriptionCore setContributor(String a, TranscriptionCore c) {
        ActorCore actor = new ActorCore();
        actor.setName(a);
        actor.setRole(new ActorRoleId("contributor"));
        return setActor(actor, c);
    }

   private static TranscriptionCore setActor(ActorCore actor, TranscriptionCore c){
        if(c.isAnalytic()){
            List<ActorCore> actors = c.getAnalyticInfo().getActors();
            if (actors == null) actors = new ArrayList<>();
            actors.add(actor);
            c.getAnalyticInfo().setActors(actors);
        }
        if(!c.isAnalytic()){
            List<ActorCore> actors = c.getMonographicInfo().getActors();
            if (actors == null) actors = new ArrayList<>();
            actors.add(actor);
            c.getMonographicInfo().setActors(actors);
        }
        return c;
    }

    static TranscriptionCore setDate(String a, TranscriptionCore c) {
        c.getMonographicInfo().setPublicationDate(a);
        return c;
    }

    static TranscriptionCore setDescription(String a, TranscriptionCore c) {
        String summary = c.getProfileDesc().getSummary();
        if (StringUtils.isBlank(summary)){
            summary = a;
        }else{
            summary = summary.concat(System.lineSeparator()+a);
        }
        c.getProfileDesc().setSummary(summary);
        return c;
    }

    static TranscriptionCore setSubject(String a, TranscriptionCore c) {
        List<String> list = c.getProfileDesc().getKeywords();
        if (list == null ) list = new ArrayList<>();
        list.add(a);
        c.getProfileDesc().setKeywords(list);
        return c;
    }

    static TranscriptionCore setIdentifier(String a, TranscriptionCore c) {

        if (StringUtils.startsWithAny(a, "https://", "http://"))
            c.setExternalUrl(a);

        if (StringUtils.startsWithIgnoreCase(a, "oai"))
            c.setExternalId(a);

        if (a.toLowerCase().startsWith("isbn".toLowerCase()))
            c.getMonographicInfo().setIsbn(a);

        if (a.toLowerCase().startsWith("eisbn".toLowerCase()))
            c.getMonographicInfo().setEisbn(a);

        if (a.toLowerCase().startsWith("issn".toLowerCase()))
            c.getMonographicInfo().setIssn(a);

        if (a.toLowerCase().startsWith("eissn".toLowerCase()))
            c.getMonographicInfo().setEissn(a);
        return c;
    }

    static TranscriptionCore setPublisher(String a, TranscriptionCore c) {
        c.getMonographicInfo().setPublisher(a);
        return c;
    }

    static TranscriptionCore setLanguage(String a, TranscriptionCore c) {
        LanguageId lang = new LanguageId();
        lang.setCode(configuration.getLanguageCode(a));
        if (lang.getCode() != null) {
            if (c.isAnalytic()) c.getAnalyticInfo().setLanguage(lang);
            if (!c.isAnalytic()) c.getMonographicInfo().setLanguage(lang);
        }
        return c;
    }

    static TranscriptionCore setAnalytic(String a, TranscriptionCore c) {
        if(!c.isAnalytic())
            c.setAnalytic(configuration.getAnalyticCharacteristics().contains(a));
        return c;
    }
}
