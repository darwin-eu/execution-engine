package eu.darwin.node.controller;

import eu.darwin.node.domain.DBMSType;
import eu.darwin.node.domain.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class DataSourceControllerTest {

    @Autowired
    DataSourceController controller;

    @Test
    void shouldCreateAndRetrieveDatasource() {
        var ds = new DataSource()
                .name("Test Data Source")
                .type(DBMSType.POSTGRESQL)
                .cdmSchema("cdmv5")
                .username("tester");
        controller.create(ds);
        var sources = controller.list();
        assertEquals(1, sources.size());
    }
}
