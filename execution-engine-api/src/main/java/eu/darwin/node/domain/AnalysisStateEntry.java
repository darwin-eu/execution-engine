package eu.darwin.node.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "analysis_state_journal")
@Embeddable
@NoArgsConstructor
public class AnalysisStateEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "date")
    private Date date;
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private AnalysisState state;
    @Column(name = "reason")
    private String reason;
    @ManyToOne()
    private Analysis analysis;


    public AnalysisStateEntry(Date date, AnalysisState state, String reason, Analysis analysis) {
        this.date = date;
        this.state = state;
        this.reason = reason;
        this.analysis = analysis;
    }


}
