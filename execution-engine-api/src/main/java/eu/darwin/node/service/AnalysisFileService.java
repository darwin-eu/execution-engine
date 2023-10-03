package eu.darwin.node.service;

import eu.darwin.node.domain.Analysis;
import eu.darwin.node.domain.AnalysisFile;
import eu.darwin.node.exceptions.ExecutionEngineExeception;
import eu.darwin.node.repo.AnalysisFileRepo;
import eu.darwin.node.util.AnalysisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AnalysisFileService {

    private final AnalysisFileRepo repo;
    private final LogService logService;


    public List<AnalysisFile> getAnalysisResults(Analysis analysis) {
        return repo.findAllByAnalysisIdAndType(
                analysis.id(),
                AnalysisFile.Type.RESULT);
    }

    public File getAnalysisSubmissionArchive(Analysis analysis) {
        var list = repo.findAllByAnalysisIdAndType(
                analysis.id(),
                AnalysisFile.Type.ANALYSIS).stream().filter(f -> f.link().endsWith("archive")).toList();
        if (list.size() == 1) {
            return new File(list.get(0).link());
        } else {
            throw new ExecutionEngineExeception("Found " + list.size() + " archive files, this should not be possible");
        }
    }

    public void persistResultFiles(Analysis analysis, File dir) {
        int resultFilesCnt = AnalysisUtils.getDirectoryItems(dir).size();
        logService.info("Analysis results zip contains " + resultFilesCnt + " files", analysis);
        List<AnalysisFile> resultFiles = Arrays.stream(dir.listFiles())
                .map(file -> new AnalysisFile(file.getAbsolutePath(), AnalysisFile.Type.RESULT, analysis))
                .toList();
        repo.saveAll(resultFiles);
    }


}
