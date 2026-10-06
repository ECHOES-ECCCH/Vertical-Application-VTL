package pl.psnc.vtl.portal.services.imports.tei.initializers;

import pl.psnc.vtl.portal.dto.transcriptions.ProfileDescCore;

import java.util.ArrayList;

public class ProfileDescCoreInitializer {

    private ProfileDescCore profileDescCore;

    public ProfileDescCoreInitializer() {
    }

    public void init(ProfileDescCore profileDescCore){
        this.profileDescCore = profileDescCore;
        initKeywords();
        initLinks();
    }

    private void initKeywords(){
        this.profileDescCore.setKeywords(new ArrayList<>());
    }

    private void initLinks(){
        this.profileDescCore.setLinks(new ArrayList<>());
    }
}
