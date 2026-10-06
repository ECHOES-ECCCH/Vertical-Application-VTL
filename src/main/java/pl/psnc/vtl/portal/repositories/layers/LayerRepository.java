package pl.psnc.vtl.portal.repositories.layers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.layers.Layer;

import java.util.Collection;
import java.util.List;

@Repository
public interface LayerRepository extends JpaRepository<Layer, Long> {

    int countLayersByTranscriptionId(@Param("transcriptionId") Long transcriptionId);

    List<Layer> findAllByTranscriptionIdOrderById(@Param("transcriptionId") Long transcriptionId);

    List<Layer> findAllByTranscriptionIdIn(Collection<Long> transcriptionIds);

    Layer findFirstByTranscriptionIdOrderById(@Param("transcriptionId") Long transcriptionId);

}
