package pl.psnc.vtl.ocrtraining.domain.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;


@Data
@Builder
@AllArgsConstructor
@ToString
class TrainedModelInfo {
    private String relativePath;
    private String langCode;
}
