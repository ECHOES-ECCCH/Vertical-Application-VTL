package pl.psnc.vtl.iiif.services.image.filename;

import lombok.Getter;

@Getter
public class BucketCannotBeDividedException extends Exception {

    private final int urlIndex;

    public BucketCannotBeDividedException(int urlIndex) {
        this.urlIndex = urlIndex;
    }

}
