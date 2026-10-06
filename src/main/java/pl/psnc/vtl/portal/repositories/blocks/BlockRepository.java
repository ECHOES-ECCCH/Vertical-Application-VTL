package pl.psnc.vtl.portal.repositories.blocks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.blocks.Block;
import pl.psnc.vtl.portal.model.blocks.BlockType;
import pl.psnc.vtl.portal.model.layers.PageLayer;

import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    List<Block> getByPageLayer(PageLayer pageLayer);

    List<Block> getByPageLayerId(PageLayer.Id pageLayer);

    List<Block> getByPageLayerIdAndContinuation(PageLayer.Id pageLayer, boolean continuation);

    List<Block> getByPageLayerIdAndType(PageLayer.Id pageLayer, BlockType type);

    void deleteByPageLayerId(PageLayer.Id pageLayer);
}
