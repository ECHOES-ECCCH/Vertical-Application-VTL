package pl.psnc.vtl.iiif.eBooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.multipart.MultipartFile;
import pl.psnc.vtl.iiif.converters.ConversionException;
import pl.psnc.vtl.iiif.converters.HtmlContent;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

class EpubCreator {

    Book getBook(MultipartFile[] files, String title, String author, String jsonMapping)
            throws IOException {

        if (files.length == 0) {
            throw new ConversionException("Cannot generate empty epub (got 0 files)!");
        }

        Map<String, String> iiifServerUrlsMap = new HashMap<>();
        if(jsonMapping != null) {
            ObjectMapper mapper = new ObjectMapper();
            iiifServerUrlsMap = mapper.readValue(jsonMapping, Map.class);
        }

        Book book = new Book();

        book.getMetadata().addTitle(title);
        book.getMetadata().addAuthor(new Author(author));

        Deque<TOCReference> sections = new ArrayDeque<>();
        Deque<Integer> levels = new ArrayDeque<>();

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            int level = getParagraphLevel(fileName);
            HtmlContent content = getHtmlContent(file, level);

            while (!levels.isEmpty() && level <= levels.peek()) {
                sections.pop();
                levels.pop();
            }

            if (sections.isEmpty()) {
                sections.push(book.addSection(
                        content.getFileHeader(), new Resource(content.getContent(), fileName)));
            } else {
                sections.push(book.addSection(sections.peek(),
                        content.getFileHeader(), new Resource(content.getContent(), fileName)));
            }
            levels.push(level);

            handleImages(file, iiifServerUrlsMap, book);
        }
        return book;
    }


    private void handleImages(MultipartFile file, Map<String, String> iiifServerUrlsMap, Book book)
            throws IOException {
        for (String urlName : getUrlsNames(file)) {
            try {
                URL urlTmp = new URL(iiifServerUrlsMap.get(urlName));
                HttpURLConnection connection = (HttpURLConnection) urlTmp.openConnection();
                try (InputStream inputStream = connection.getInputStream()) {
                    book.getResources().add(new Resource(inputStream, urlName));
                } finally {
                    connection.disconnect();
                }
            }
            catch (Exception e) {
                throw new ConversionException("Cannot handle image");
            }
        }
    }


    private String getFileHeader(String htmlContent, int headerNo) {
        Document html = Jsoup.parse(htmlContent);
        return html.body().getElementsByTag("h" + headerNo).text();
    }


    private List<String> getUrlsNames(MultipartFile file)
            throws IOException {
        BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
        bis.mark(0);
        bis.reset();
        StringWriter writer = new StringWriter();
        IOUtils.copy(bis, writer, StandardCharsets.UTF_8);
        String htmlContent = writer.toString();

        Document html = Jsoup.parse(htmlContent);
        List<String> result = new ArrayList<>();
        Elements elements = html.body().getElementsByTag("img");
        for (Element e : elements) {
            String url = e.attr("src");
            result.add(url);
        }
        return result;
    }


    private int getParagraphLevel(String filename) {
        return Integer.parseInt(filename.substring(0, 1));
    }


    private HtmlContent getHtmlContent(MultipartFile file, int headerLevel)
            throws IOException {
        BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
        byte[] bytes = bis.readAllBytes();

        String htmlContent = new String(bytes, StandardCharsets.UTF_8);
        String fileHeader = getFileHeader(htmlContent, headerLevel);

        return new HtmlContent(fileHeader, bytes);
    }
}
