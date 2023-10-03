package eu.darwin.node.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "analyses")
@Getter
@Setter
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "executable_file_name")
    private String executableFileName;
    @ManyToOne
    private DataSource dataSource;
    @Column(name = "analysis_folder")
    private String analysisFolder;
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "analysis")
    private List<AnalysisStateEntry> stateHistory = new ArrayList<>();
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "analysis")
    private List<AnalysisFile> analysisFiles = new ArrayList<>();
    @OneToMany(cascade = {CascadeType.REFRESH}, mappedBy = "analysis", fetch = FetchType.LAZY)
    private List<Log> logs = new ArrayList<>();
    @Column(name = "result_status")
    @Enumerated(EnumType.STRING)
    private AnalysisState status;
    @Column(name = "study_title")
    private String studyTitle;
    @Column(name = "study_id")
    private String studyId;
    @Column(name = "execution_environment")
    private String executionEnvironment;
    @Column(name = "comment")
    private String comment;
    @ManyToOne
    private User user;
    private Date created;
    private Date finished;
    private String checksum;
    @JdbcTypeCode(SqlTypes.JSON)
    private List<StudyParameters> customParams;

    public void addLog(Log entry) {
        if (logs == null) {
            logs = new ArrayList<>();
        }
        logs.add(entry);
    }

    // Must be a mutable list otherwise hibernate goes belly up
    public void setAnalysisFiles(List<AnalysisFile> analysisFiles) {
        this.analysisFiles = new ArrayList<>(analysisFiles);
    }

    public User getUser() {
        if (user == null) {
            return User.anonymous();
        }
        return user;
    }

}


