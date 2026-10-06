package pl.psnc.vtl.iiif.services.image;

import pl.psnc.vtl.iiif.services.image.filename.BucketCannotBeDividedException;
import pl.psnc.vtl.iiif.services.image.filename.UrlsBucket;
import pl.psnc.vtl.iiif.services.image.filename.UrlsBucketComparator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FilenamesExtractor {

    private final List<String> urls;


    public FilenamesExtractor(List<String> urls) {
        this.urls = urls;
    }


    public List<String> getFilenames() throws NameCannotBeExtractedException {
        UrlsBucket initialBucket = new UrlsBucket(validate());
        Collection<UrlsBucket> urlsBuckets;
        try {
            urlsBuckets = initialBucket.divide();
            while (urlsBuckets.size() < urls.size()) {
                List<UrlsBucket> subbuckets = new ArrayList<>();
                for (UrlsBucket urlsBucket : urlsBuckets) {
                    subbuckets.addAll(urlsBucket.divide());
                }
                urlsBuckets = subbuckets;
            }
        } catch (BucketCannotBeDividedException e) {
            throw new NameCannotBeExtractedException("Filename from " + urls.get(e.getUrlIndex()) + " cannot be extracted!");
        }
        List<UrlsBucket> buckets = new ArrayList<>(urlsBuckets);
        buckets.sort(new UrlsBucketComparator());
        return buckets.stream().map(UrlsBucket::getFilename).collect(Collectors.toList());
    }

    private List<URL> validate() throws NameCannotBeExtractedException {
        List<URL> urls = new ArrayList<>();
        for (String url : this.urls) {
            try {
                url = url.replace("/?IIIF=", "/");
                url = url.replace("?IIIF=", "/");
                urls.add(new URL(url));
            } catch (MalformedURLException e) {
                throw new NameCannotBeExtractedException("Filename from " + url + " cannot be extracted!");
            }
        }
        return urls;
    }

}
