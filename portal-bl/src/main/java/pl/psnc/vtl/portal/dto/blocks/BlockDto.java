package pl.psnc.vtl.portal.dto.blocks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.blocks.Block;
import pl.psnc.vtl.portal.model.blocks.BlockType;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockDto {

    @Null
    private Long blockId;

    @NotNull
    private BlockType type;

    @Min(1)
    @NotNull
    private Integer firstVerseNo;

    @NotNull
    private Integer lastVerseNo;

    private boolean continuation;

    private boolean potentialContinuation;

    private boolean continued;

    /** Instances of BlockDto or Verse **/
    private List<Object> children;

    private EnumSet<BlockConsistencyError> consistencyErrors;

    public BlockDto(BlockType type, List<? extends Object> children) {
        this.type = type;
        this.children = new ArrayList<>(children);
    }

    public void addConsistencyError(BlockConsistencyError error) {
        if (consistencyErrors == null)
            consistencyErrors = EnumSet.noneOf(BlockConsistencyError.class);
        consistencyErrors.add(error);
    }

    public static BlockDto from(Block b) {
        return new BlockDto(b.getId(), b.getType(), b.getFirstVerseNo(), b.getLastVerseNo(),
                b.isContinuation(), false, false, new ArrayList<>(), null);
    }

    public static List<BlockDto> onlyBlocks(List<? extends Object> itmes) {
        return itmes.stream().filter(BlockDto.class::isInstance).map(BlockDto.class::cast).collect(toList());
    }
}
