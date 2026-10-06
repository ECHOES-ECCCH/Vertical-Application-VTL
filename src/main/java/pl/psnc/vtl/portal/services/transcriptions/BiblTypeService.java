package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.model.transcriptions.BiblType;
import pl.psnc.vtl.portal.repositories.transcriptions.BiblTypeRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BiblTypeService {

    private final BiblTypeRepository biblTypeRepository;


    public List<BiblType> getBiblTypes() {
        return biblTypeRepository.findAll(Sort.by("ord"));
    }

}
