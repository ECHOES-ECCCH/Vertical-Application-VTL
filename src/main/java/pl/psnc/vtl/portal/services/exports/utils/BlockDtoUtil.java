package pl.psnc.vtl.portal.services.exports.utils;

import lombok.experimental.UtilityClass;
import pl.psnc.vtl.portal.dto.blocks.BlockDto;
import pl.psnc.vtl.portal.model.blocks.BlockType;
import pl.psnc.vtl.portal.model.verses.Verse;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BlockDtoUtil {

    public static Verse firstVerse(BlockDto blockDto) {
        Object firstChild = blockDto.getChildren().get(0);
        return (firstChild instanceof Verse) ? (Verse) firstChild : firstVerse((BlockDto) firstChild);
    }

    public static List<Verse> allVerses(BlockDto blockDto) {
        List<Verse> result = new ArrayList<>();
        allVersesRec(blockDto, result);
        return result;
    }

    private void allVersesRec(BlockDto blockDto, List<Verse> result) {
        for (Object child : blockDto.getChildren()) {
            if (child instanceof Verse) {
                result.add((Verse) child);
            } else {
                allVersesRec((BlockDto) child, result);
            }
        }
    }

    public static Iterable<BlockDto> blocksOfType(BlockType type, BlockDto blockDto) {
        return () -> blockDto.getChildren().stream()
                .filter(BlockDto.class::isInstance).map(BlockDto.class::cast)
                .filter(b -> b.getType() == type).iterator();
    }
}
