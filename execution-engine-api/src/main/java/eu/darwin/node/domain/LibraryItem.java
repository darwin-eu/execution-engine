package eu.darwin.node.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class LibraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Date created;
    private String link;
    private String entrypoint;
    private String engine;
    @JdbcTypeCode(SqlTypes.JSON)
    private List<StudyParameters> params;

    @PrePersist
    private void onCreate() {
        if (created == null) {
            created = new Date();
        }
    }

}
