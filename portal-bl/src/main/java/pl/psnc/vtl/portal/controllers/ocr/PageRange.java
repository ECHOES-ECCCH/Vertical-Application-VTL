package pl.psnc.vtl.portal.controllers.ocr;

import lombok.Data;

@Data
public class PageRange {

    private Integer from;
    private Integer to;

    public boolean isPageInRange(int pageNumber) {
        return (from == null || pageNumber >= from) && (to == null || pageNumber <= to);
    }

}
