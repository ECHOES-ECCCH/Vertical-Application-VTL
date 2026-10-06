package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Paginated transcriptions with owner object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaginatedTranscriptionsWithOwner<T> extends PaginatedTranscriptions<T> {

    /**
     * Email of transcriptions owner.
     */
    private String ownerEmail;

}
