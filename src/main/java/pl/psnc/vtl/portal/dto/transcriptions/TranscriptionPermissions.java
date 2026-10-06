package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.model.permissions.TranscriptionGroup;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUser;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

import java.util.List;

/**
 * Holds the information about permissions to the selected transcription
 */
@AllArgsConstructor
@Builder
@Getter
public class TranscriptionPermissions {

    /**
     * Is transcription public.
     */
    private final boolean publiclyAvailable;

    /**
     * Transcription reference.
     */
    private final Transcription transcription;

    /**
     * Users allowed to view this transcription.
     */
    private final List<TranscriptionUser> allowedUsers;

    /**
     * Groups allowed to view this transcription.
     */
    private final List<TranscriptionGroup> allowedGroups;

    /**
     * Current user view mode of this transcription.
     */
    private final ViewMode viewMode;

    /**
     * Transcription continuation.
     */
    private TranscriptionContinuation transcriptionContinuation;

}
