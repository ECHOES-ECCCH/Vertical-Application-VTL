package pl.psnc.vtl.iiif.services.image.filename;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Data
public class UrlsBucket {

    private List<UrlIndex> splitUrls;

    public UrlsBucket(List<URL> urls) {
        this.splitUrls = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            String[] splitUrl = urls.get(i).getPath().split("/");
            String filename = constructFilenameFromPathPart(splitUrl[splitUrl.length - 1]);
            this.splitUrls.add(new UrlIndex(i, splitUrl, filename, FilenameUtils.getExtension(filename)));
        }
    }

    private UrlsBucket(int index, String[] url, String name, String ext) {
        this.splitUrls = new ArrayList<>();
        this.splitUrls.add(new UrlIndex(index, url, name, ext));
    }

    private void addUrl(int index, String[] url, String name, String ext) {
        this.splitUrls.add(new UrlIndex(index, url, name, ext));
    }

    private String constructFilenameFromPathPart(String pathPart) {
        return StringUtils.stripAccents(URLDecoder.decode(pathPart, StandardCharsets.UTF_8).replace(" ", "_"));
    }

    public Collection<UrlsBucket> divide() throws BucketCannotBeDividedException {
        if (this.getSplitUrls().size() == 1) {
            return Collections.singletonList(this);
        }
        Map<String, UrlsBucket> subbuckets = new HashMap<>();
        for (UrlIndex splitUrl: splitUrls) {
            String lastUrlPart = splitUrl.getLastUrlPart();
            String filename = constructFilenameFromPathPart(lastUrlPart);
            if (subbuckets.get(filename) != null) {
                subbuckets.get(filename).addUrl(splitUrl.getIndex(), splitUrl.getUrlStart(), filename, splitUrl.getExt());
            } else {
                subbuckets.put(filename, new UrlsBucket(splitUrl.getIndex(), splitUrl.getUrlStart(), filename, splitUrl.getExt()));
            }
        }
        return subbuckets.values();
    }

    public String getFilename() {
        UrlIndex splitUrl = splitUrls.get(0);
        // if the extension is already in the file path part, then return this part, otherwise add extension as we
        // are retrieving format indicated by extension in the url
        if (Objects.equals(FilenameUtils.getExtension(splitUrl.getName()), splitUrl.getExt())) {
            return splitUrl.getName();
        } else {
            return splitUrl.getName() + "." + splitUrl.getExt();
        }
    }


    @Data
    @AllArgsConstructor
    public class UrlIndex {

        private int index;

        private String[] url;

        private String name;

        private String ext;

        public String getLastUrlPart() throws BucketCannotBeDividedException {
            if (url.length == 0) {
                throw new BucketCannotBeDividedException(index);
            }
            return url[url.length - 1];
        }

        public String[] getUrlStart() {
            return Arrays.copyOfRange(url, 0, url.length - 1);
        }

    }

}
