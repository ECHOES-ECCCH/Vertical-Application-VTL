package pl.psnc.vtl.portal.dto.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class TranscriptionPages {

    private final Transcription transcription;
    private final List<Page> pages = new ArrayList<>();
}
