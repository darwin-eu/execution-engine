package eu.darwin.node.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Container {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Our internal db id
    private String containerId; // Docker assigned id
    @Enumerated(EnumType.STRING)
    private Status status;
    @OneToOne
    private Analysis analysis;


    public enum Status {
        RUNNING,
        FINISHED,
        CANCELLED_BY_USER
    }

}
