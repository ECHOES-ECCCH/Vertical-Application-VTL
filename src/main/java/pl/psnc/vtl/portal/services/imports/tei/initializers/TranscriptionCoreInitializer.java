package pl.psnc.vtl.portal.services.imports.tei.initializers;

import pl.psnc.vtl.portal.dto.transcriptions.*;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionInfo;

public class TranscriptionCoreInitializer {

    private AnalyticInfoCoreInitializer analyticInfoCoreInitializer;

    private MonographicInfoCoreInitializer monographicInfoCoreInitializer;

    private  ProfileDescCoreInitializer profileDescCoreInitializer;

    public TranscriptionCoreInitializer(){
        this.analyticInfoCoreInitializer = new AnalyticInfoCoreInitializer();
        this.monographicInfoCoreInitializer = new MonographicInfoCoreInitializer();
        this.profileDescCoreInitializer = new ProfileDescCoreInitializer();
    }

    public void initialize(TranscriptionCore transcriptionCore){
        transcriptionCore.setAnalyticInfo(new AnalyticInfoCore());
        transcriptionCore.setMonographicInfo(new MonographicInfoCore());
        transcriptionCore.setProfileDesc(new ProfileDescCore());
        transcriptionCore.setTranscriptionInfo(new TranscriptionInfoCore());
        this.analyticInfoCoreInitializer.init(transcriptionCore.getAnalyticInfo());
        this.monographicInfoCoreInitializer.init(transcriptionCore.getMonographicInfo());
        this.profileDescCoreInitializer.init(transcriptionCore.getProfileDesc());
    }


}
