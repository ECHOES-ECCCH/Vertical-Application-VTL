package pl.psnc.vtl.portal.model.blocks;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;

public enum BlockType {

    HEADING_1,
    HEADING_2,
    HEADING_3,

    PARAGRAPH,

    TABLE,
    TABLE_ROW,
    TABLE_CELL,

    LIST,
    LIST_ITEM,

    ANNOTATION,
    CAPTION,
    /**
     * Block of type image: should contain one verse and one block of type caption
     * (in any order).
     */
    IMAGE,

    // TEI
    HEADER,
    FOOTER,
    PAGE_NO,
    ADD,
    ADD_ABOVE,
    ADD_BELOW,
    ADD_MARGIN,
    DEL,
    SUBST,

    DIV_PROLOGUE,
    DIV_PART,
    DIV_CHAPTER,
    DIV_SECTION;

    public static final Map<BlockType, BlockType> FORCED_RELATIONS = Map.of(
            TABLE, TABLE_ROW,
            TABLE_ROW, TABLE_CELL,
            LIST, LIST_ITEM,
            IMAGE, CAPTION);

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

}
