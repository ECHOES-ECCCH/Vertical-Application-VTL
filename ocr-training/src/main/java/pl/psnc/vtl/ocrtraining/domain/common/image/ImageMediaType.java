package pl.psnc.vtl.ocrtraining.domain.common.image;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public enum ImageMediaType {
    JPEG("jpeg"),
    PNG("png");

    @Getter
    private String fileExtension;
}
