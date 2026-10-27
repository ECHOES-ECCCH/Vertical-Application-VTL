package pl.psnc.vtl.portal.services.indexes;

import org.mapstruct.factory.Mappers;
import pl.psnc.vtl.portal.dto.indexes.IndexDto;
import pl.psnc.vtl.portal.model.indexes.Index;

import java.util.List;

public interface IndexMapper {

    IndexMapper INSTANCE = Mappers.getMapper(IndexMapper.class);

    IndexDto toDto(Index index);

    List<IndexDto> toDto(List<Index> indices);
}
