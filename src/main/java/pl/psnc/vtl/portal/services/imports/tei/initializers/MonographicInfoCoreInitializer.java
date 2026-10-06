package pl.psnc.vtl.portal.services.imports.tei.initializers;

import pl.psnc.vtl.portal.dto.transcriptions.MonographicInfoCore;

import java.util.ArrayList;

public class MonographicInfoCoreInitializer {

    private MonographicInfoCore monographicInfoCore;

    public MonographicInfoCoreInitializer() {
    }

    public void init (MonographicInfoCore monographicInfoCore){
        this.monographicInfoCore = monographicInfoCore;
        initActors();
    }

    private void initActors(){
        this.monographicInfoCore.setActors(new ArrayList<>());
    }

}
