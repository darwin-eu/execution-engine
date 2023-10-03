package eu.darwin.node.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.darwin.node.domain.*;
import eu.darwin.node.dto.SubmissionRequestDTO;
import eu.darwin.node.dto.SubmissionResultMetaDataDTO;
import eu.darwin.node.exceptions.ExecutionEngineExeception;
import eu.darwin.node.repo.AnalysisRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static eu.darwin.node.domain.AnalysisState.FAILED;
import static eu.darwin.node.util.ZipUtil.unzipFiles;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalysisService {

    public static final String ZIP_FILE_NAME = "analysis.zip";
    private final AnalysisRepo repo;
    private final FileService fileService;
    private final DataSourceService dataSourceService;
    private final AnalysisStateService analysisStateService;
    private final DockerService dockerService;
    private final AnalysisFileService analysisFileService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final RuntimeService runtimeService;
    private final LogService logService;
    private final AnalysisResultsService resultsService;


    public List<Analysis> findAll() {
        return repo.findAll();
    }

    public Analysis byId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Analysis with id: [" + id + "] not found"));
    }

    public File copyAnalysisFiles(Analysis analysis, Long toRerunId) {
        try {
            File analysisDir = new File(analysis.analysisFolder());
            File zipDir = fileService.makeZipDir(analysisDir);
            var original = byId(toRerunId);
            var originalZipDir = analysisFileService.getAnalysisSubmissionArchive(original);
            FileUtils.copyDirectory(originalZipDir, zipDir);
            File archiveFile = new File(zipDir, ZIP_FILE_NAME);
            analysis.checksum(fileService.checksum(archiveFile));
            unzipFiles(archiveFile, analysisDir);
            return process(analysis, analysisDir);
        } catch (IOException e) {
            analysisStateService.failed(analysis, e.getMessage());
            log.error(e.getMessage());
            throw new ExecutionEngineExeception("Failed to copy analysis files");
        }
    }

    public File copyAnalysisFilesFromLibraryItem(Analysis analysis, LibraryItem libraryItem) {
        try {
            var analysisDir = new File(analysis.analysisFolder());
            var zipDir = fileService.makeZipDir(analysisDir);
            var zipFile = new File(zipDir, ZIP_FILE_NAME);
            FileUtils.copyFile(new File(libraryItem.link()), zipFile);
            analysis.checksum(fileService.checksum(zipFile));
            unzipFiles(zipFile, analysisDir);
            return process(analysis, analysisDir);
        } catch (IOException e) {
            analysisStateService.failed(analysis, e.getMessage());
            log.error(e.getMessage());
            throw new ExecutionEngineExeception("Failed to copy analysis files from library");
        }
    }


    public void writeStdOutToDir(Analysis analysis, File tmpdir) {
        var file = new File(tmpdir, "stdout.txt");
        var stdout = analysis.logs().stream().sorted(Comparator.comparing(Log::date)).map(Log::line).collect(Collectors.joining("\n"));
        write(file, stdout);
    }

    private void write(File file, String text) {
        try (var writer = new FileWriter(file)) {
            writer.write(text);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ExecutionEngineExeception("Failed to write text to file");
        }
    }

    public void writeMetaDataFileToDir(Analysis analysis, File dir) throws IOException {
        var file = new File(dir, "metadata.json");
        var metadata = SubmissionResultMetaDataDTO.fromAnalysis(analysis);
        var text = objectMapper.writeValueAsString(metadata);
        write(file, text);
    }

    public void resultFilesToDir(Analysis analysis, File tmpdir) {
        List<AnalysisFile> resultFiles = analysisFileService.getAnalysisResults(analysis);
        resultFiles.forEach(f -> {
            try {
                FileUtils.copyToDirectory(new File(f.link()), tmpdir);
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new ExecutionEngineExeception("Failed to copy result files to directory");
            }
        });
    }

    public File saveAnalysisFiles(Analysis analysis, MultipartFile files) {
        try {
            final File analysisDir = new File(analysis.analysisFolder());
            var archiveFile = fileService.saveMultipartToZip(analysisDir, files);
            unzipFiles(archiveFile, analysisDir);
            analysis.checksum(fileService.checksum(archiveFile));
            return process(analysis, analysisDir);
        } catch (IOException e) {
            analysisStateService.failed(analysis, e.getMessage());
            log.error(e.toString());
            throw new ExecutionEngineExeception("Failed to save analysis files");
        }
    }


    private File process(Analysis analysis, File analysisDir) {
        File[] filesList = analysisDir.listFiles();
        if (Objects.nonNull(filesList)) {
            List<AnalysisFile> analysisFiles = Arrays.stream(filesList)
                    .map(f -> new AnalysisFile()
                            .analysis(analysis)
                            .type(AnalysisFile.Type.ANALYSIS)
                            .link(f.getPath())).toList();
            analysis.setAnalysisFiles(analysisFiles);
        }
        repo.save(analysis);
        return analysisDir;
    }

    public Analysis processSubmissionRequest(SubmissionRequestDTO dto) {
        var user = user();
        var analysisFolder = fileService.createDir().getAbsolutePath();
        var dataSource = dataSourceService.findById(dto.datasourceId());
        var analysis = new Analysis()
                .created(new Date())
                .user(user)
                .executableFileName(dto.entrypoint())
                .executionEnvironment(dto.engine())
                .analysisFolder(analysisFolder)
                .studyTitle(dto.studyTitle())
                .studyId(dto.studyId())
                .customParams(dto.params())
                .dataSource(dataSource);
        repo.save(analysis);
        analysisStateService.created(analysis);
        return analysis;
    }

    private User user() {
        return userService.current();
    }

    @Async
    public void sendToEngine(File analysisDir, Analysis analysis) {
        try {
            var resultStatus = runtimeService.analyze(analysis, analysisDir);
            resultsService.processAnalysisResult(analysis, resultStatus, analysisDir);
        } catch (Exception e) {
            log.error(e.toString());
            logService.error("analysis with id= " + analysis.id() + " failed to execute " + e.getMessage(), analysis);
            resultsService.processAnalysisResult(analysis, FAILED, analysisDir);
        }
    }

    public Analysis saveComment(Long analysisId, String comment) {
        var a = byId(analysisId);
        a.comment(comment);
        return repo.save(a);
    }

    public void cancel(Long id) {
        var a = byId(id);
        dockerService.cancel(a);
        analysisStateService.cancelled(a, user());
    }
}
