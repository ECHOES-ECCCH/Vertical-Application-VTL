package pl.psnc.vtl.iiif.eBooks;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.TOCReference;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EpubCreatorTest {

    private EpubCreator epubCreator = new EpubCreator();

    private MultipartFileGenerator multipartFileGenerator = new MultipartFileGenerator();


    @Test
    public void shouldCreateBookWithOneSubsection()
            throws IOException {
        List<MultipartFile> files = new ArrayList<>();

        files.add(multipartFileGenerator
                .generateMultipartFile("src/main/resources/epubTestHtml/1fileNo2.html", "1fileNo1.html"));
        files.add(multipartFileGenerator
                .generateMultipartFile("src/main/resources/epubTestHtml/2fileNo3.html", "2fileNo2.html"));

        MultipartFile[] filesArray = new MultipartFile[files.size()];
        filesArray = files.toArray(filesArray);
        String title = "some Title";
        String author = "some author";

        Book book = epubCreator.getBook(filesArray, title, author, null);

        assertEquals(title, book.getTitle());

        String exceptedAuthor = author + ", ";
        assertEquals(exceptedAuthor.toString(), book.getMetadata().getAuthors().get(0).toString());

        List<TOCReference> tocReferences = book.getTableOfContents().getTocReferences();

        assertEquals(1, tocReferences.get(0).getChildren().size());
        assertEquals("Introduction 1", tocReferences.get(0).getTitle());

        assertEquals(0, tocReferences.get(0).getChildren().get(0).getChildren().size());

    }


    @Test
    public void shouldCreateBookWhenHasChapterSectionSubsectionAngSecondChapterWithSectionAndSubsection()
            throws IOException {
        List<MultipartFile> files = new ArrayList<>();
        files.add(multipartFileGenerator
                .generateMultipartFile("src/main/resources/epubTestHtml/1fileNo2.html", "1fileNo1.html"));
        files.add(multipartFileGenerator
                .generateMultipartFile("src/main/resources/epubTestHtml/2fileNo3.html", "2fileNo2.html"));
        files.add(multipartFileGenerator
                .generateMultipartFile("src/main/resources/epubTestHtml/3fileNo4.html", "3fileNo3.html"));
        files.add(multipartFileGenerator
                .generateMultipartFile("src/main/resources/epubTestHtml/1fileNo8.html", "1fileNo4.html"));
        files.add(multipartFileGenerator
                .generateMultipartFile("src/main/resources/epubTestHtml/2fileNo9.html", "2fileNo5.html"));
        files.add(multipartFileGenerator
                .generateMultipartFile("src/main/resources/epubTestHtml/3fileNo11.html", "3fileNo6.html"));

        MultipartFile[] filesArray = new MultipartFile[files.size()];
        filesArray = files.toArray(filesArray);
        String title = "some Title";
        String author = "some author";

        Book book = epubCreator.getBook(filesArray, title, author, null);

        assertEquals(title, book.getTitle());

        String exceptedAuthor = author + ", ";
        assertEquals(exceptedAuthor.toString(), book.getMetadata().getAuthors().get(0).toString());

        List<TOCReference> tocReferences = book.getTableOfContents().getTocReferences();

        assertEquals(1, tocReferences.get(0).getChildren().size());
        assertEquals("Introduction 1", tocReferences.get(0).getTitle());

        assertEquals(1, tocReferences.get(0).getChildren().get(0).getChildren().size());
        assertEquals("Jakis naglowek 1", tocReferences.get(0).getChildren().get(0).getTitle());
        assertEquals("Jakas subsekcja", tocReferences.get(0).getChildren().get(0).getChildren().get(0).getTitle());

        assertEquals(1, tocReferences.get(1).getChildren().size());
        assertEquals("Introduction 2", tocReferences.get(1).getTitle());

        assertEquals(1, tocReferences.get(1).getChildren().get(0).getChildren().size());
        assertEquals("Section h2", tocReferences.get(1).getChildren().get(0).getTitle());
        assertEquals("another header 3", tocReferences.get(1).getChildren().get(0).getChildren().get(0).getTitle());
    }


    @Test
    public void shouldCreateBookWhenHasOneSectionAndTwoSubsection()
            throws IOException {
        List<MultipartFile> files = new ArrayList<>();
        files.add(multipartFileGenerator
                .generateMultipartFile("src/main/resources/epubTestHtml/1fileNo2.html", "1fileNo1.html"));
        files.add(multipartFileGenerator
                .generateMultipartFile("src/main/resources/epubTestHtml/2fileNo3.html", "2fileNo2.html"));
        files.add(multipartFileGenerator
                .generateMultipartFile("src/main/resources/epubTestHtml/3fileNo4.html", "3fileNo3.html"));
        files.add(multipartFileGenerator
                .generateMultipartFile("src/main/resources/epubTestHtml/3fileNo5.html", "3fileNo4.html"));

        MultipartFile[] filesArray = new MultipartFile[files.size()];
        filesArray = files.toArray(filesArray);
        String title = "some Title";
        String author = "some author";

        Book book = epubCreator.getBook(filesArray, title, author, null);

        assertEquals(title, book.getTitle());

        String exceptedAuthor = author + ", ";
        assertEquals(exceptedAuthor.toString(), book.getMetadata().getAuthors().get(0).toString());

        List<TOCReference> tocReferences = book.getTableOfContents().getTocReferences();

        assertEquals(1, tocReferences.get(0).getChildren().size());
        assertEquals("Introduction 1", tocReferences.get(0).getTitle());

        assertEquals(2, tocReferences.get(0).getChildren().get(0).getChildren().size());
        assertEquals("Jakis naglowek 1", tocReferences.get(0).getChildren().get(0).getTitle());
        assertEquals("Jakas subsekcja", tocReferences.get(0).getChildren().get(0).getChildren().get(0).getTitle());
        assertEquals("Subsekcja", tocReferences.get(0).getChildren().get(0).getChildren().get(1).getTitle());

    }


    @Test
    public void shouldCreateBookWhenHasOneFile()
            throws IOException {
        List<MultipartFile> files = new ArrayList<>();
        files.add(multipartFileGenerator
                .generateMultipartFile("src/main/resources/epubTestHtml/1fileNo1.html", "1fileNo1.html"));

        MultipartFile[] filesArray = new MultipartFile[files.size()];
        filesArray = files.toArray(filesArray);
        String title = "some Title";
        String author = "some author";

        Book book = epubCreator.getBook(filesArray, title, author, null);

        assertEquals(title, book.getTitle());

        String exceptedAuthor = author + ", ";
        assertEquals(exceptedAuthor.toString(), book.getMetadata().getAuthors().get(0).toString());

        List<TOCReference> tocReferences = book.getTableOfContents().getTocReferences();

        assertEquals(0, tocReferences.get(0).getChildren().size());
        assertEquals("Introduction", tocReferences.get(0).getTitle());
    }

}