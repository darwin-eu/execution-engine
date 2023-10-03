package eu.darwin.node.dto;

import eu.darwin.node.domain.DBMSType;
import eu.darwin.node.domain.DataSource;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataSourceDTO {

    private Long id;
    private String name;
    private String description;
    private DBMSType type;
    private String connectionString;
    private String cdmSchema;
    private String username;
    private String targetSchema;
    private String resultSchema;
    private String cohortTargetTable;
    private String dbServer;
    private String dbName;
    private String dbPort;
    private String dbCatalog;
    private String cdmVersion; /* This can only be 5.3 or 5.4 so perhaps should be an enum */

    public static DataSourceDTO fromEntity(DataSource entity) {
        return new DataSourceDTO()
                .id(entity.id())
                .name(entity.name())
                .description(entity.description())
                .type(entity.type())
                .connectionString(entity.connectionString())
                .dbServer(entity.dbServer())
                .dbName(entity.dbName())
                .dbPort(entity.dbPort())
                .cdmVersion(entity.cdmVersion())
                .cdmSchema(entity.cdmSchema())
                .username(entity.username())
                .targetSchema(entity.targetSchema())
                .resultSchema(entity.resultSchema())
                .cohortTargetTable(entity.cohortTargetTable())
                .dbCatalog(entity.dbCatalog());
    }

}
