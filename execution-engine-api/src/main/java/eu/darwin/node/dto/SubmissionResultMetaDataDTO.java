package eu.darwin.node.dto;

import eu.darwin.node.domain.Analysis;
import eu.darwin.node.domain.StudyParameters;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubmissionResultMetaDataDTO {

    private String comment;
    private String database;
    private String start;
    private String end;
    private String image;
    private String studyId;
    private String checksum;
    private String user;
    private List<StudyParameters> params;


    public static SubmissionResultMetaDataDTO fromAnalysis(Analysis analysis) {
        return new SubmissionResultMetaDataDTO()
                .comment(analysis.comment())
                .database(analysis.dataSource().name())
                .start(analysis.created().toString())
                .end(analysis.finished().toString())
                .image(analysis.executionEnvironment())
                .studyId(analysis.studyId())
                .checksum(analysis.checksum())
                .params(analysis.customParams())
                .user(analysis.getUser().username());
    }

}
