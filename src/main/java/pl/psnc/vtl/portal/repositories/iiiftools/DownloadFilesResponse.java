package pl.psnc.vtl.portal.repositories.iiiftools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadFilesResponse {

    private Map<String, String> urlsToFilenames;
    private long quota;
}
