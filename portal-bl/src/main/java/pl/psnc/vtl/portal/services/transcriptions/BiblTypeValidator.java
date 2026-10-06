package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import pl.psnc.vtl.portal.dto.transcriptions.BiblTypeId;
import pl.psnc.vtl.portal.model.transcriptions.BiblType;
import pl.psnc.vtl.portal.repositories.transcriptions.BiblTypeRepository;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BiblTypeValidator {

    private final BiblTypeRepository biblTypeRepository;


    public BiblType validate(BiblTypeId biblTypeId, Errors errors) {
        if (biblTypeId != null) {
            Optional<BiblType> biblTypeHolder = biblTypeRepository.findById(biblTypeId.getCode());
            if (biblTypeHolder.isEmpty()) {
                if (errors != null) {
                    errors.rejectValue("code", "transcriptions.error" + TranscriptionErrorMessages.FIELD_REQUIRED.getMessage());
                }
            } else {
                return biblTypeHolder.get();
            }
        }
        return null;
    }

}
