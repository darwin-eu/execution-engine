package eu.darwin.node.dto;

import eu.darwin.node.domain.StudyParameters;

import java.util.List;

public record SubmissionRequestDTO(String entrypoint,
                                   Long datasourceId,
                                   String studyId,
                                   String studyTitle,
                                   String engine,
                                   List<StudyParameters> params) {
}
