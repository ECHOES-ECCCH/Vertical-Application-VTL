package pl.psnc.vtl.portal.services.indexes;

import pl.psnc.vtl.portal.dto.indexes.IndexDto;
import pl.psnc.vtl.portal.model.indexes.Index;
import pl.psnc.vtl.portal.services.pages.PageMapper;

import java.util.ArrayList;
import java.util.List;

public class IndexMapperImpl implements IndexMapper {

    @Override
    public IndexDto toDto(Index index) {
        if (index == null) {
            return null;
        }

        IndexDto indexDto = new IndexDto();

        indexDto.setId(index.getId());
        indexDto.setName(index.getName());
        indexDto.setAccessibleOn(index.getAccessibleOn());
        indexDto.setDescription(index.getDescription());
        indexDto.setType(index.getType());

        if (index.getVerses() != null) {
            index.getVerses().forEach(verse -> {
                indexDto.addVerseId(verse.getId());
                indexDto.addPageDto(PageMapper.INSTANCE.toDto(verse.getPage()));
            });
        }
        if (index.getWords() != null) {
            index.getWords().forEach(word -> {
                indexDto.addWordId(word.getId());
            });
        }

        return indexDto;
    }


    @Override
    public List<IndexDto> toDto(List<Index> indices) {
        if (indices == null) {
            return null;
        }

        List<IndexDto> list = new ArrayList<>(indices.size());
        for (Index index : indices) {
            list.add(toDto(index));
        }

        return list;
    }
}
