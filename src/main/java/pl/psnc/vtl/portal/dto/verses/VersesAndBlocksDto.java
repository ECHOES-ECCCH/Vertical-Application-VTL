package pl.psnc.vtl.portal.dto.verses;

import lombok.Data;
import pl.psnc.vtl.portal.model.verses.Verse;

import java.util.List;

@Data
public class VersesAndBlocksDto {
    private List<Verse> verses;
    private List<Object> blocks;
}
