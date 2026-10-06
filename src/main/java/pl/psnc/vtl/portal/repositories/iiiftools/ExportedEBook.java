package pl.psnc.vtl.portal.repositories.iiiftools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.services.eBook.Status;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class ExportedEBook {
    Status status;
    String details;
}
