package eu.darwin.node.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.sql.Driver;
import java.sql.DriverManager;

@Configuration
@Slf4j
public class DriverManagerConfig {

    @PostConstruct
    public void logLoadedDrivers() {
        try {
            Class.forName("org.netezza.Driver");
        } catch (Exception e) {
            // ignore
        }
        java.util.Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver d = drivers.nextElement();
            log.info("Loaded JDBC driver: {}. Version {}.{} ", d.getClass().getName(), d.getMajorVersion(), d.getMinorVersion());
        }
    }
}
