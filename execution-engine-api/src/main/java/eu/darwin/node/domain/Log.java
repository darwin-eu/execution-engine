package eu.darwin.node.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String line;
    @ManyToOne(fetch = FetchType.LAZY)
    private Analysis analysis;
    private String level;
    private Date date;

    public Log(String line, Analysis analysis, String level) {
        this.line = line;
        this.analysis = analysis;
        this.level = level;
        this.date = new Date();
    }

}
