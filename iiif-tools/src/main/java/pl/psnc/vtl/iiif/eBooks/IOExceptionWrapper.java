package pl.psnc.vtl.iiif.eBooks;

import java.io.IOException;

class IOExceptionWrapper extends RuntimeException {

    IOExceptionWrapper(IOException e) {
        super(e.getMessage());
    }

}
