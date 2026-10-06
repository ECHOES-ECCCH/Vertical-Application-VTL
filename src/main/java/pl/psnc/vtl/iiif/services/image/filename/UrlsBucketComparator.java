package pl.psnc.vtl.iiif.services.image.filename;

import pl.psnc.vtl.iiif.services.image.filename.UrlsBucket;

import java.util.Comparator;

public class UrlsBucketComparator implements Comparator<UrlsBucket> {

    @Override
    public int compare(UrlsBucket urlsBucket1, UrlsBucket urlsBucket2) {
        return urlsBucket1.getSplitUrls().get(0).getIndex() - urlsBucket2.getSplitUrls().get(0).getIndex();
    }

}
