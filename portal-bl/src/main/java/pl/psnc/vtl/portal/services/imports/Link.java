package pl.psnc.vtl.portal.services.imports;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Link {


    public Link(String url){
        this.url = url;
    }

    private String url;
    private Integer width;
    private Integer height;
    private String thumbnailUrl;
    private String infoJsonUrl;


}
