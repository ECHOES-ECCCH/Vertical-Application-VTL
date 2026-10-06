package pl.psnc.vtl.ocrtraining.domain.training;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.psnc.vtl.ocrtraining.domain.common.file.FileSystemRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
class FileSystemTransaction {

    private final FileSystemRepository fs;
    private final List<Path> savedFiles = new ArrayList<>();
    private final Map<Path, Path> backedFiles = new HashMap<>();
    private boolean committed = false;

    public void saveNewFile(byte[] contents, Path destination) throws IOException {
        fs.writeBytesToFile(contents, destination);
        savedFiles.add(destination);
    }

    public void updateFile(byte[] contents, Path destination) throws IOException {
        Path backupPath = Path.of(destination.toString() + ".bak");
        fs.copyFile(destination, backupPath);
        backedFiles.put(backupPath, destination);
        fs.writeBytesToFile(contents, destination);
    }

    public void commit() {
        if (committed)
            return;
        for (Path backupPath : backedFiles.keySet()) {
            try {
                fs.deleteFile(backupPath);
            } catch (IOException e) {
                log.warn(String.format("Failed to delete backup file (path = %s) due to an IO error",
                        backupPath.toString()), e);
            }
        }
        this.committed = true;
    }

    public void rollback() {
        if (committed)
            return;

        for (Map.Entry<Path, Path> updatedPaths : backedFiles.entrySet())
            rollbackUpdate(updatedPaths.getValue(), updatedPaths.getKey());

        savedFiles.forEach(this::rollbackSave);
    }

    private void rollbackUpdate(Path originalPath, Path backupPath) {
        try {
            fs.copyFile(backupPath, originalPath);
            fs.deleteFile(backupPath);
        } catch (IOException e) {
            log.warn(String.format("Failed to restore modified ground truth text file (path = %s) due to an IO error",
                    originalPath.toString()), e);
        }
    }

    private void rollbackSave(Path imagePath) {
        try {
            fs.deleteFile(imagePath);
        } catch (IOException e) {
            log.warn(String.format("Failed to rollback (delete) saved image file (path = %s) due to an error",
                    imagePath.toString()), e);
        }
    }
}
