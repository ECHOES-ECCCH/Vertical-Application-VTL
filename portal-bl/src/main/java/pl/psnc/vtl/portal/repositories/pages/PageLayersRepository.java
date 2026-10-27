package pl.psnc.vtl.portal.repositories.pages;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.layers.PageLayer;

import java.util.List;
import java.util.Optional;


@Repository
public interface PageLayersRepository extends JpaRepository<PageLayer, PageLayer.Id> {

    Optional<PageLayer> findByPageIdAndLayerId(Long pageId, Long layerId);

    void deleteByLayerId(long layerId);

    List<PageLayer> findAllByLayerId(Long layerId);

    List<PageLayer> findAllByPageId(Long pageId);

}
