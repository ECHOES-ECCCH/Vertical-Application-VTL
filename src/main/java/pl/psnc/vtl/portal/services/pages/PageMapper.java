package pl.psnc.vtl.portal.services.pages;

import org.mapstruct.factory.Mappers;
import pl.psnc.vtl.portal.dto.pages.PageDto;
import pl.psnc.vtl.portal.model.pages.Page;

import java.util.List;

public interface PageMapper {

    PageMapper INSTANCE = Mappers.getMapper(PageMapper.class);

    PageDto toDto(Page page);

    List<PageDto> toDto(List<Page> pages);

}
