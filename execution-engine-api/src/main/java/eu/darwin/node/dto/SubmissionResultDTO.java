package eu.darwin.node.dto;

import eu.darwin.node.domain.Analysis;
import eu.darwin.node.domain.Log;
import eu.darwin.node.domain.StudyParameters;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class SubmissionResultDTO {
    private Long id;
    private String studyTitle;
    private String studyId;
    private String dataSource;
    private Long datasourceId;
    private String status;
    private String user;
    private Date created;
    private Date finished;
    private String comment;
    private List<LogDTO> logs;
    private String entrypoint;
    private String engine;
    private List<StudyParameters> params;


    public static SubmissionResultDTO fromAnalysis(Analysis analysis) {
        return fromAnalysis(analysis, Collections.emptyList());
    }

    public static SubmissionResultDTO fromAnalysis(Analysis analysis, List<Log> logs) {
        SubmissionResultDTO dto = new SubmissionResultDTO()
                .studyTitle(analysis.studyTitle())
                .id(analysis.id())
                .studyId(analysis.studyId());
        if (analysis.dataSource() == null) {
            dto.dataSource("deleted datasource");
            dto.datasourceId(0L);
        } else {
            dto.dataSource(analysis.dataSource().name());
            dto.datasourceId(analysis.dataSource().id());
        }
        var state = analysis.status() == null ? "Something has gone wrong..." : analysis.status().toString();
        return dto.status(state)
                .user(analysis.getUser().username())
                .created(analysis.created())
                .finished(analysis.finished())
                .comment(analysis.comment())
                .logs(logs.stream().map(LogDTO::fromEntity).toList())
                .engine(analysis.executionEnvironment())
                .params(analysis.customParams())
                .entrypoint(analysis.executableFileName());
    }

}
