package eu.darwin.node.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import eu.darwin.node.domain.Analysis;
import eu.darwin.node.domain.AnalysisState;
import eu.darwin.node.domain.DataSource;
import eu.darwin.node.domain.StudyParameters;
import eu.darwin.node.util.FileResourceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static eu.darwin.node.domain.AnalysisState.EXECUTED;
import static eu.darwin.node.domain.AnalysisState.FAILED;

@Service
@Slf4j
@RequiredArgsConstructor
public class RuntimeService {

    private static final String RUNTIME_ENV_DATA_SOURCE_NAME = "DATA_SOURCE_NAME";
    private static final String RUNTIME_ENV_DBMS_USERNAME = "DBMS_USERNAME";
    private static final String RUNTIME_ENV_DBMS_PASSWORD = "DBMS_PASSWORD";
    private static final String RUNTIME_ENV_DBMS_TYPE = "DBMS_TYPE";
    private static final String RUNTIME_ENV_CONNECTION_STRING = "CONNECTION_STRING";
    private static final String RUNTIME_ENV_CDM_SCHEMA = "CDM_SCHEMA";
    private static final String RUNTIME_ENV_WRITE_SCHEMA = "WRITE_SCHEMA";
    private static final String RUNTIME_ENV_RESULT_SCHEMA = "RESULT_SCHEMA";
    private static final String RUNTIME_ENV_DB_CATALOG = "DBMS_CATALOG";
    private static final String RUNTIME_ENV_DB_SERVER = "DBMS_SERVER";
    private static final String RUNTIME_ENV_DB_NAME = "DBMS_NAME";
    private static final String RUNTIME_ENV_DB_PORT = "DBMS_PORT";
    private static final String RUNTIME_ENV_CMD_VERSION = "CDM_VERSION";
    private static final String RUNTIME_ENV_COHORT_TARGET_TABLE = "COHORT_TARGET_TABLE";
    private static final String RUNTIME_ENV_PATH_KEY = "PATH";
    private static final String RUNTIME_ENV_PATH_VALUE = "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin";
    private static final String RUNTIME_ENV_LANG_KEY = "LANG";
    private static final String RUNTIME_ENV_LANG_VALUE = "en_US.UTF-8";
    private static final String RUNTIME_ENV_LC_ALL_KEY = "LC_ALL";
    private static final String RUNTIME_ENV_LC_ALL_VALUE = "en_US.UTF-8";
    private static final String RUNTIME_ANALYSIS_ID = "ANALYSIS_ID";
    private final DockerService dockerService;
    private final LogService logService;
    private final AnalysisStateService stateService;

    public AnalysisState analyze(Analysis analysis, File analysisDir) {
        var imageName = analysis.executionEnvironment();
        dockerService.pullImageIfNotExists(imageName, analysis);
        try (DockerClient dockerClient = dockerService.dockerClient()) {
            logService.info("Execution engine is creating a Docker container from image: " + imageName, analysis);
            var containerId = createContainer(dockerClient, analysis);
            stateService.executing(analysis);
            long statusCode = runContainer(dockerClient, containerId, analysisDir, analysis);
            logService.info("Removing container", analysis);
            dockerService.remove(dockerClient, containerId);
            return statusCode == 0 ? EXECUTED : FAILED;
        } catch (Exception e) {
            logService.info("Something went sour running Docker... " + e.getMessage(), analysis);
            log.error(e.toString());
            return FAILED;
        }
    }

    private String createContainer(DockerClient dockerClient, Analysis analysis) {
        var imageName = analysis.executionEnvironment();
        log.info("Creating container for image: {}", imageName);
        var env = buildRuntimeEnvVariables(analysis);
        List<String> e = new ArrayList<>(env.size());
        env.forEach((k, v) -> e.add(k + "=" + v));
        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withEnv(e)
                .withStdinOpen(true)
                .withTty(true)
                .exec();
        dockerService.register(analysis, container.getId());
        log.info("Container created for image {} with container id {}", imageName, container.getId());
        return container.getId();
    }

    private long runContainer(DockerClient dockerClient, String containerId, File analysisDir, Analysis analysis) {
        var execFileName = analysis.executableFileName();

        log.info("Starting container: {}", containerId);
        dockerClient.startContainerCmd(containerId).exec();

        log.info("Copying study into container: {}", containerId);
        dockerClient.copyArchiveToContainerCmd(containerId)
                .withHostResource(analysisDir.getPath())
                .withRemotePath("/code").exec();

        String baseDir = "/code/" + analysisDir.getName() + "/";
        String workingDir = baseDir + (execFileName.split("/").length > 1 ? execFileName.split("/")[0] : "");
        log.info("Working dir {}", workingDir);
        String rscriptPath = baseDir + execFileName;
        log.info("Script path {}", rscriptPath);
        var runCommandId = dockerClient.execCreateCmd(containerId)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd("Rscript", rscriptPath)
                .withWorkingDir(workingDir).exec()
                .getId();

        ResultCallback.Adapter<Frame> adapter = getAdapter(analysis);
        try {
            log.info("Running script: {}", rscriptPath);
            dockerClient.execStartCmd(runCommandId).exec(adapter).awaitCompletion();
        } catch (InterruptedException e) {
            dockerService.stop(dockerClient, containerId);
            log.error("InterruptedException occurred whilst running {} in {}, message: {}", rscriptPath, containerId, e.getMessage());
            return 1;
        }
        long exitCode = dockerClient.inspectExecCmd(runCommandId).exec().getExitCodeLong();
        logService.info("Execution of R script finished with status code: " + exitCode, analysis);
        if (exitCode == 0) {
            logService.info("Extracting results from container", analysis);
            exitCode = extractResultsFromContainer(dockerClient, containerId, analysisDir);
        }
        dockerService.stop(dockerClient, containerId);
        return exitCode;
    }

    private Integer extractResultsFromContainer(DockerClient dockerClient, String containerId, File analysisDir) {
        log.info("Extracting results from container with id: {}", containerId);
        try (var tis = new TarArchiveInputStream(dockerClient.copyArchiveFromContainerCmd(containerId, "/results").exec())) {
            FileResourceUtils.unpack(tis, analysisDir);
            return 0;
        } catch (IOException e) {
            log.error("IOException occurred whilst copying results from container {} message: {}", containerId, e.getMessage());
            dockerService.stop(dockerClient, containerId);
            return 1;
        }
    }


    private ResultCallback.Adapter<Frame> getAdapter(Analysis analysis) {
        return new ResultCallback.Adapter<>() {
            @Override
            public void onNext(Frame item) {
                logService.info(item.toString(), analysis);
            }
        };
    }

    private Map<String, String> buildRuntimeEnvVariables(Analysis analysis) {
        log.debug("[{}] Building runtime env variables", analysis.id());
        DataSource dataSource = analysis.dataSource();
        String analysisRequestId = analysis.id().toString();

        Map<String, String> environment = new HashMap<>();
        if (analysis.customParams() != null) {
            var params = analysis.customParams().stream()
                    .filter(sp -> sp.value() != null)
                    .collect(Collectors.toMap(StudyParameters::key, p -> p.value().toString()));
            environment.putAll(params);
        }

        environment.put(RUNTIME_ENV_DATA_SOURCE_NAME, dataSource.name());
        environment.put(RUNTIME_ENV_DBMS_USERNAME, dataSource.username());
        environment.put(RUNTIME_ENV_DBMS_PASSWORD, dataSource.password());
        environment.put(RUNTIME_ENV_DBMS_TYPE, dataSource.type().ohdsiDB());
        environment.put(RUNTIME_ENV_CONNECTION_STRING, dataSource.connectionString());
        environment.put(RUNTIME_ENV_CDM_SCHEMA, dataSource.cdmSchema());
        environment.put(RUNTIME_ENV_WRITE_SCHEMA, dataSource.writeSchema());
        environment.put(RUNTIME_ENV_RESULT_SCHEMA, dataSource.resultSchema());
        environment.put(RUNTIME_ENV_DB_CATALOG, dataSource.dbCatalog());
        environment.put(RUNTIME_ENV_DB_SERVER, dataSource.dbServer());
        environment.put(RUNTIME_ENV_DB_NAME, dataSource.dbName());
        environment.put(RUNTIME_ENV_DB_PORT, dataSource.dbPort());
        environment.put(RUNTIME_ENV_CMD_VERSION, dataSource.cdmVersion());
        environment.put(RUNTIME_ENV_COHORT_TARGET_TABLE, dataSource.cohortTargetTable());
        environment.put(RUNTIME_ENV_PATH_KEY, RUNTIME_ENV_PATH_VALUE);
        environment.put(RUNTIME_ENV_LANG_KEY, RUNTIME_ENV_LANG_VALUE);
        environment.put(RUNTIME_ENV_LC_ALL_KEY, RUNTIME_ENV_LC_ALL_VALUE);
        environment.put(RUNTIME_ANALYSIS_ID, analysisRequestId);

        environment.values().removeIf(Objects::isNull);
        return environment;
    }
}
