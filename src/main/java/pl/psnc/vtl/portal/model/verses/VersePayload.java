package pl.psnc.vtl.portal.model.verses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Basic;
import javax.persistence.FetchType;

/**
 * <p>Describes payload for the given verse. It can be any array of bytes.</p>
 * <p>For now it will be used only for small xml files with music notation</p>
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VersePayload {

    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    private byte[] payload;

    private String payloadFileName;

    private String payloadContentType;

}
