package eu.darwin.node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StudyExecutionEngine {

    public static void main(String[] args) {
        SpringApplication.run(StudyExecutionEngine.class, args);
    }

}
