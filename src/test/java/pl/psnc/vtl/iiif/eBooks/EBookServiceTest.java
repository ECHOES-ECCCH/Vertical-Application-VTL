package pl.psnc.vtl.iiif.eBooks;


import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

/**
 * use it to test IO passing your own location
 */
public class EBookServiceTest {

    private String rootFolder = new File("src/test/resources").getAbsolutePath();

    private String eBooksPhysicalFolder = EBookLocation.eBooksFolderPhysicalPath(rootFolder);

    @SneakyThrows
    @After
    public void clean() {
        Path path = Path.of(eBooksPhysicalFolder);
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        new File(eBooksPhysicalFolder).mkdirs();
    }


    @Test
    public void shouldDeleteOldFilesButSkippResent() {
        //given
        oldAndRecentFiles();
        //and
        ScheduledCleanUpProcess scheduledCleanUpProcess =
                new ScheduledCleanUpProcess(rootFolder, Duration.ofSeconds(1));
        //when
        scheduledCleanUpProcess.cleanStorage();
        //then
        Assert.assertTrue(recentFilesExist());
        //and
        Assert.assertFalse(oldFilesExist());

    }

    private boolean oldFilesExist() {
        return oldFiles.stream()
                .map(FilePath::getFullName)
                .map(eBooksPhysicalFolder::concat)
                .map(Paths::get)
                .filter(Files::exists)
                .anyMatch(Files::exists);
    }

    private boolean recentFilesExist() {
        long count = recentFiles.stream()
                .map(FilePath::getFullName)
                .map(eBooksPhysicalFolder::concat)
                .map(Paths::get)
                .filter(Files::exists)
                .count();
        return count == 6L;
    }

    @SneakyThrows
    private void oldAndRecentFiles() {
        createFiles(oldFiles);
        Thread.sleep(3000);
        createFiles(recentFiles);
    }

    @Getter
    final class FilePath {
        final String path;
        final String fullName;

        FilePath(String path, String fullName) {
            this.path = path;
            this.fullName = path.concat(fullName);
        }
    }


    private void createFiles(List<FilePath> files) {
        files.stream()
                .map(FilePath::getPath)
                .map(eBooksPhysicalFolder::concat)
                .map(Paths::get)
                .map(Path::toFile)
                .forEach(File::mkdirs);

        files.stream()
                .map(FilePath::getFullName)
                .map(eBooksPhysicalFolder::concat)
                .map(Paths::get)
                .map(Path::toFile)
                .forEach(this::createFile);
    }

    @SneakyThrows
    private void createFile(File file) {
        file.createNewFile();
    }

    private List<FilePath> recentFiles =
        List.of(
                new FilePath("/t1/epub", "/01.epub"),
                new FilePath("/t1/mobi", "/01.mobi"),
                new FilePath("/t1/pdf", "/01.pdf"),
                new FilePath("/t2/epub", "/01.epub"),
                new FilePath("/t2/pdf", "/01.epub"),
                new FilePath("/t2/mobi", "/01.mobi"));

    private List<FilePath> oldFiles =
        List.of(
                new FilePath("/t1/epub", "/1.epub"),
                new FilePath("/t1/mobi", "/1.mobi"),
                new FilePath("/t1/pdf", "/1.pdf"),
                new FilePath("/t2/epub", "/1.epub"),
                new FilePath("/t2/pdf", "/1.epub"),
                new FilePath("/t2/mobi", "/1.mobi"),
                new FilePath("/t1/epub", "/2.epub"),
                new FilePath("/t1/mobi", "/2.mobi"),
                new FilePath("/t1/pdf", "/2.pdf"),
                new FilePath("/t2/epub", "/2.epub"),
                new FilePath("/t2/pdf", "/2.epub"),
                new FilePath("/t2/mobi", "/2.mobi"));

}