package eu.darwin.node.service;

import eu.darwin.node.domain.Analysis;
import eu.darwin.node.domain.AnalysisState;
import eu.darwin.node.exceptions.ExecutionEngineExeception;
import eu.darwin.node.repo.AnalysisRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static eu.darwin.node.domain.AnalysisState.EXECUTED;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AnalysisResultsService {

    private static final String ERROR_REPORT_FILENAME_LOWER = "errorreport.txt";
    private static final String ERROR_REPORT_R_FILENAME_LOWER = "errorreportr.txt";
    private final AnalysisFileService analysisFileService;
    private final AnalysisRepo analysisRepository;
    private final AnalysisStateService analysisStateService;
    private final WebSocketService webSocketService;


    public void processAnalysisResult(Analysis analysis, AnalysisState status, File dir) {
        analysisStateService.update(analysis, status, "Saving analysis results");
        analysisFileService.persistResultFiles(analysis, dir);
        updateAnalysisWithResultsData(analysis, dir);
    }


    private void updateAnalysisWithResultsData(Analysis analysis, File resultDir) {
        reEvaluateAnalysisStatus(analysis, resultDir);
        analysis.analysisFolder(resultDir.getAbsolutePath());
        webSocketService.updateAnalysis(analysis);
        analysisRepository.save(analysis);
    }

    private void reEvaluateAnalysisStatus(Analysis analysis, File resultDir) {
        if (EXECUTED == analysis.status()) {
            if (resultDir == null) {
                analysisStateService.failed(analysis, "Result directory cannot be null");
            } else {
                File[] zipFiles = resultDir.listFiles((dir, name) -> name.endsWith(".zip"));
                if (checkZipArchiveForErrorFile(zipFiles)) {
                    analysisStateService.failed(analysis, "Unexpected errorReport file found. Changing analysis status to FAILED for " + resultDir);
                }
            }
        }
    }

    private boolean checkZipArchiveForErrorFile(File[] listFiles) {
        return Stream.of(listFiles)
                .map(this::scanZipForErrorFilenames)
                .reduce(Boolean::logicalOr)
                .orElse(false);
    }

    private boolean scanZipForErrorFilenames(File zipFile) {
        try (ZipFile archive = new ZipFile(zipFile)) {
            return archive.stream()
                    .map(ZipEntry::getName)
                    .map(String::toLowerCase)
                    .anyMatch(name -> name.endsWith(ERROR_REPORT_FILENAME_LOWER) || name.endsWith(ERROR_REPORT_R_FILENAME_LOWER));
        } catch (IOException ex) {
            log.error(ex.getMessage());
            throw new ExecutionEngineExeception("Failed to scan zip file");
        }
    }
}
