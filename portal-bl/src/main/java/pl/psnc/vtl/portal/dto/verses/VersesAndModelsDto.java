package pl.psnc.vtl.portal.dto.verses;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

import java.util.List;

@Data
@Validated
public class VersesAndModelsDto {
    @NotEmpty
    private List<Long> verseIds;
    @NotEmpty
    private String modelIds;
}
