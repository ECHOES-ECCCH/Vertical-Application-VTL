package pl.psnc.vtl.portal.dto.word;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeiElementDto {

    @NotEmpty
    private List<Long> wordsIdentifiers;
    @NotNull
    private TeiElement teiElement;
    @Schema(description = "For TEI ADD this value will be placed in `place` attribute (above, below, margin),<br/>" +
            "For TEI SUBST this value will be used as identifier for element (any string) ")
    private String teiElementValue;

    public enum TeiElement {
        ADD,
        DEL,
        GAP,
        SUBST
    }
}
