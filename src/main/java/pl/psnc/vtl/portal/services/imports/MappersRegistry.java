package pl.psnc.vtl.portal.services.imports;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.imports.FileType;
import pl.psnc.vtl.portal.services.imports.dace.DaceMapperStrategy;
import pl.psnc.vtl.portal.services.imports.iiif.IIIFMapperStrategy;
import pl.psnc.vtl.portal.services.imports.mets.DublinCoreMapperStrategy;
import pl.psnc.vtl.portal.services.imports.tei.TeiMapperStrategy;

import java.util.HashMap;
import java.util.Map;

@Service
class MappersRegistry {

    private final Map<FileType, MapperStrategy> registry;

    public MappersRegistry(@Autowired MappingConfiguration mappingConfiguration){
        /*
            initialize registry
            maybe with simple constructors passing required beans or through @Autowired for simplicity
         */
        registry = new HashMap<>();
        registry.put(FileType.METS, new DublinCoreMapperStrategy());
        registry.put(FileType.TEI, new TeiMapperStrategy());
        registry.put(FileType.IIIF, new IIIFMapperStrategy());
        registry.put(FileType.DACE, new DaceMapperStrategy(mappingConfiguration));
    }

    public MapperStrategy getMapperStrategy(FileType fileType) {
        return registry.get(fileType);
    }
}
