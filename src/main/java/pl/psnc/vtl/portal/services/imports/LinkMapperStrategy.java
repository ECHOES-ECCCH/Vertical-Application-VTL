package pl.psnc.vtl.portal.services.imports;

public interface LinkMapperStrategy {

    PublicationLinks getTranscriptionLinks(LoadedFile loadedFile);
}
