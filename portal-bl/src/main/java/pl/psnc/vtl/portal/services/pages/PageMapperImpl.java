package pl.psnc.vtl.portal.services.pages;

import pl.psnc.vtl.portal.dto.pages.PageDto;
import pl.psnc.vtl.portal.model.pages.Page;

import java.util.ArrayList;
import java.util.List;

public class PageMapperImpl implements PageMapper {

    @Override
    public PageDto toDto(Page page) {
        if (page == null) {
            return null;
        }

        PageDto pageDto = new PageDto();
        pageDto.setId(page.getId());
        pageDto.setPageNo(page.getPageNo());

        return pageDto;
    }


    @Override
    public List<PageDto> toDto(List<Page> pages) {
        if (pages == null) {
            return null;
        }

        List<PageDto> list = new ArrayList<>(pages.size());
        for (Page page : pages) {
            list.add(toDto(page));
        }

        return list;
    }
}
