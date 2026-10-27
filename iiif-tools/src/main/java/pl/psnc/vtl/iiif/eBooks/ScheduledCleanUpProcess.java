package pl.psnc.vtl.iiif.eBooks;

import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Comparator;
import java.util.stream.Stream;

public class ScheduledCleanUpProcess {

    private final String eBooksFolder;
    private final TemporalAmount maxEBookAge;

    public ScheduledCleanUpProcess(String rootFolder) {
        this(rootFolder, Period.ofWeeks(1));
    }

    public ScheduledCleanUpProcess(String rootFolder, TemporalAmount maxEBookAge) {
        this.eBooksFolder = EBookLocation.eBooksFolderPhysicalPath(rootFolder);
        this.maxEBookAge = maxEBookAge;
    }

    @Scheduled(fixedRateString = "${eBooks.storage.clean-up-rate}")
    void cleanStorage() {
        Path path = Path.of(eBooksFolder);
        listChildFiles(path)
                .filter(Files::isDirectory)
                .flatMap(this::listChildFiles)
                .forEach(this::deleteOldFilesSkippingTheMostRecent);
    }

    private boolean isOlderThanOneMaxEBookAge(Path path) {
        return getCreationTime(path).toInstant().isBefore(Instant.now().minus(maxEBookAge));
    }

    private void deleteOldFilesSkippingTheMostRecent(Path path) {
        listChildFiles(path)
                .sorted(Comparator.comparing(this::getCreationTime).reversed())
                .skip(1)
                .filter(this::isOlderThanOneMaxEBookAge)
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @SneakyThrows
    private FileTime getCreationTime(Path path) {
        return Files.readAttributes(path, BasicFileAttributes.class).creationTime();
    }

    @SneakyThrows
    private Stream<Path> listChildFiles(Path path) {
        return Files.list(path);
    }
}
