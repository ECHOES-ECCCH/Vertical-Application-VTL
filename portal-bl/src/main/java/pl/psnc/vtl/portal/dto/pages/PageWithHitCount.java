package pl.psnc.vtl.portal.dto.pages;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.convert.ReadingConverter;
import pl.psnc.vtl.portal.model.pages.Page;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;

/**
 * Page with hit count object.
 */
@Data
@AllArgsConstructor
public class PageWithHitCount {

    /**
     * Page reference.
     */
    private Page page;

    /**
     * Search hit count on given page.
     */
    private Long hitCount;
}
