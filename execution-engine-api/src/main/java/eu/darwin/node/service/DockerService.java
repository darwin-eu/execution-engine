package eu.darwin.node.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import eu.darwin.node.domain.Analysis;
import eu.darwin.node.domain.Container;
import eu.darwin.node.exceptions.ExecutionEngineExeception;
import eu.darwin.node.repo.ContainerRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class DockerService {


    private final LogService logService;
    private final ContainerRepo containerRepo;
    @Value("${docker.registry.url:#{null}}")
    private String registryUrl;
    @Value("${docker.registry.username:#{null}}")
    private String registryUsername;
    @Value("${docker.registry.password:#{null}}")
    private String registryPassword;


    public List<String> listImages(DockerClient client) {
        var images = client.listImagesCmd().exec();
        if (images == null) {
            return Collections.emptyList();
        }
        return images.stream().map(Image::getRepoTags).filter(Objects::nonNull).flatMap(Arrays::stream).toList();
    }

    public void pullImageIfNotExists(String imageName, Analysis analysis) {
        try (var client = dockerClient()) {
            var available = listImages(client);
            var exists = available.stream().anyMatch(n -> n.equals(imageName));
            if (!exists) {
                logService.info("Image " + imageName + " not found locally", analysis);
                pullImage(client, imageName, analysis);
                logService.info("Image " + imageName + " downloaded", analysis);
            }
        } catch (IOException e) {
            log.error(e.toString());
            throw new ExecutionEngineExeception("Failed to pull image if not exists");
        }
    }

    public void pullImage(DockerClient client, String imageName, Analysis analysis) {
        logService.info("Downloading " + imageName + " ... this may take some time, but we only need to do it once", analysis);
        PullImageResultCallback callback = new PullImageResultCallback() {
            @Override
            public void onError(Throwable throwable) {
                logService.error("Failed to pull image" + throwable.getMessage(), analysis);
                super.onError(throwable);
            }
        };
        try {
            client.pullImageCmd(imageName).exec(callback).awaitCompletion();
        } catch (InterruptedException ex) {
            log.error(ex.toString());
            throw new ExecutionEngineExeception("Failed to pull image");
        }
    }

    public DockerClient dockerClient() {
        var builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerTlsVerify(false);
        if (registryUrl != null) {
            builder.withRegistryUrl(registryUrl);
        }
        if (registryPassword != null) {
            builder.withRegistryPassword(registryPassword);
        }
        if (registryUsername != null) {
            builder.withRegistryUsername(registryUsername);
        }
        var config = builder.build();
        DockerHttpClient dockerHttpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(50)
                .build();
        return DockerClientImpl.getInstance(config, dockerHttpClient);
    }

    public void cancel(Analysis analysis) {
        var c = containerRepo.findFirstByAnalysis(analysis)
                .orElseThrow(() -> new ExecutionEngineExeception("No container found for analysis " + analysis.id()));
        try (var client = dockerClient()) {
            client.stopContainerCmd(c.containerId()).exec();
            client.removeContainerCmd(c.containerId()).exec();
            c.status(Container.Status.CANCELLED_BY_USER);
        } catch (IOException e) {
            log.error(e.toString());
            throw new ExecutionEngineExeception("Something went wrong stopping container");
        }
    }

    public void register(Analysis analysis, String id) {
        var c = new Container()
                .analysis(analysis)
                .status(Container.Status.RUNNING)
                .containerId(id);
        containerRepo.save(c);
    }

    public void stop(DockerClient dockerClient, String containerId) {
        log.info("Stopping container: {}", containerId);
        dockerClient.stopContainerCmd(containerId).exec();
        setFinished(containerId);
    }

    public void remove(DockerClient dockerClient, String containerId) {
        dockerClient.removeContainerCmd(containerId)
                .withRemoveVolumes(!log.isDebugEnabled())
                .exec();
        setFinished(containerId);
    }

    private void setFinished(String containerId) {
        var c = containerRepo.findFirstByContainerId(containerId).orElseThrow();
        c.status(Container.Status.FINISHED);
        containerRepo.save(c);
    }
}
