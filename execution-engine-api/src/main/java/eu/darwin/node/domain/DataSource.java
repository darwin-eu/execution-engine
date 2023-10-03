package eu.darwin.node.domain;

import eu.darwin.node.util.enc.Encrypt;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "data_source")
@Getter
@Setter
public class DataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private DBMSType type;
    private String connectionString;
    private String cdmSchema;
    private String username;
    @Convert(converter = Encrypt.class)
    private String password;
    private String targetSchema;
    private String resultSchema;
    private String cohortTargetTable;
    private String dbServer;
    private String dbName;
    private String dbPort;
    private String dbCatalog;
    private String cdmVersion; /* This can only be 5.3 or 5.4 so perhaps should be an enum */
}
