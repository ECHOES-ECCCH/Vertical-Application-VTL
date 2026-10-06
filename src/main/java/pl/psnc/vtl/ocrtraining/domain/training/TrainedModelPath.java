package pl.psnc.vtl.ocrtraining.domain.training;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;


@Getter
@Builder
@RequiredArgsConstructor
class TrainedModelPath {
    private final Path absolutePath;
    private final Path relativeSharedPath;
}
