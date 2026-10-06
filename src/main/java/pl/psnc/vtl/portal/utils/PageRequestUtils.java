package pl.psnc.vtl.portal.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.portal.repositories.verses.StatsSortField;

@Component
public class PageRequestUtils {
    @Value("${vtl.pagination.pages.maximal-perpage}")
    private int pagesMaxPerPage;

    public PageRequest getLimitedPageRequest(int page, int perPage, StatsSortField sortField, Sort.Direction direction) {
        validate(page, perPage);
        int limitedPerPage = limitPerPage(perPage);
        return PageRequest.of(page, limitedPerPage, Sort.by(direction, sortField.getSortFieldName()));
    }

    private void validate(int page, int perPage) {
        if (page < 0) {
            throw new IllegalArgumentException("Parameter page must be greater or equal to 0!");
        }
        if (perPage < 1) {
            throw new IllegalArgumentException("Parameter perPage must be greater than 0!");
        }
    }

    private int limitPerPage(int perPage) {
        return perPage > pagesMaxPerPage ? pagesMaxPerPage : perPage;
    }
}
