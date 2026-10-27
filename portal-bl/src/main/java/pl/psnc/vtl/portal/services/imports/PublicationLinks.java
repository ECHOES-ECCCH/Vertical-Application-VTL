package pl.psnc.vtl.portal.services.imports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class PublicationLinks {

    private final List<Link> links;
    private final boolean manifestLinks;
}
