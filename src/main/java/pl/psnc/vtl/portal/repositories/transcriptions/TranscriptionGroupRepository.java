package pl.psnc.vtl.portal.repositories.transcriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.permissions.TranscriptionGroup;
import pl.psnc.vtl.portal.model.permissions.TranscriptionGroupId;

import java.util.List;

@Repository
public interface TranscriptionGroupRepository extends JpaRepository<TranscriptionGroup, TranscriptionGroupId> {

    List<TranscriptionGroup> findByTranscriptionId(Long transcriptionId);

    List<TranscriptionGroup> findByTranscriptionIdIn(List<Long> transcriptionIds);

    List<TranscriptionGroup> findByGroupId(Long groupId);

    List<TranscriptionGroup> deleteByTranscriptionId(Long transcriptionId);

    List<TranscriptionGroup> deleteByGroupId(Long groupId);

}
