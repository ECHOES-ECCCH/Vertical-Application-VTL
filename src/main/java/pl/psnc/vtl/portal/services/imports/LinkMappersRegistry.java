package pl.psnc.vtl.portal.services.imports;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.imports.FileType;
import pl.psnc.vtl.portal.services.imports.dace.DaceMapperStrategy;
import pl.psnc.vtl.portal.services.imports.iiif.IIIFLinkMapperStrategy;
import pl.psnc.vtl.portal.services.imports.mets.DublinCoreLinkMapperStrategy;
import pl.psnc.vtl.portal.services.imports.tei.TeiLinkMapperStrategy;

import java.util.HashMap;
import java.util.Map;

@Service
public class LinkMappersRegistry {

    private final Map<FileType, LinkMapperStrategy> registry;

    public LinkMappersRegistry(@Autowired MappingConfiguration mappingConfiguration){
        /*
            initialize registry
            maybe with simple constructors passing required beans or through @Autowired for simplicity
         */

        registry = new HashMap<>();
        registry.put(FileType.IIIF, new IIIFLinkMapperStrategy());
        registry.put(FileType.METS, new DublinCoreLinkMapperStrategy());
        registry.put(FileType.TEI, new TeiLinkMapperStrategy());
        registry.put(FileType.DACE, new DaceMapperStrategy(mappingConfiguration));
    }

    public LinkMapperStrategy getMapperStrategy(FileType fileType) {
        return registry.get(fileType);
    }
}
