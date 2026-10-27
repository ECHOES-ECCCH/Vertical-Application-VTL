package pl.psnc.vtl.portal.services.imports.tei.initializers;

import pl.psnc.vtl.portal.dto.transcriptions.AnalyticInfoCore;

import java.util.ArrayList;

public class AnalyticInfoCoreInitializer {

    private AnalyticInfoCore analyticInfoCore;


    public AnalyticInfoCoreInitializer() {
    }

    public void init(AnalyticInfoCore analyticInfoCore){
        this.analyticInfoCore = analyticInfoCore;
        initActors();
    }


    private void initActors(){
        analyticInfoCore.setActors(new ArrayList<>());
    }
}
