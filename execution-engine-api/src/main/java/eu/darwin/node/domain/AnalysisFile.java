package eu.darwin.node.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "analysis_file")
@Getter
@Setter
@NoArgsConstructor
public class AnalysisFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private Type type;
    private String link;
    @ManyToOne
    private Analysis analysis;

    public AnalysisFile(String link, Type type, Analysis analysis) {
        this.link = link;
        this.type = type;
        this.analysis = analysis;
    }

    public enum Type {
        ANALYSIS,
        RESULT
    }

}
