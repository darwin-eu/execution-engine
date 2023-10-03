package eu.darwin.node.controller;

import eu.darwin.node.domain.Analysis;
import eu.darwin.node.dto.CommentDTO;
import eu.darwin.node.dto.SubmissionRequestDTO;
import eu.darwin.node.dto.SubmissionResultDTO;
import eu.darwin.node.service.AnalysisService;
import eu.darwin.node.service.LibraryService;
import eu.darwin.node.service.WebSocketService;
import eu.darwin.node.util.ZipUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


@RestController
@RequestMapping(path = "/submissions")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
public class SubmissionController {

    private final AnalysisService analysisService;
    private final LibraryService libraryService;
    private final WebSocketService webSocketService;

    @GetMapping
    public List<SubmissionResultDTO> list() {
        return analysisService.findAll().stream().map(SubmissionResultDTO::fromAnalysis).toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void execute(@RequestPart("file") List<MultipartFile> files, @RequestPart("submission") SubmissionRequestDTO submission) {
        if (files.size() != 1) {
            throw new ResponseStatusException(BAD_REQUEST, "Upload one single zip file");
        }
        var archive = files.get(0);
        var analysis = analysisService.processSubmissionRequest(submission);
        var dir = analysisService.saveAnalysisFiles(analysis, archive);
        analysisService.sendToEngine(dir, analysis);
    }

    @PostMapping(path = "{id}/comments")
    public void saveComment(@PathVariable("id") Long analysisId, @RequestBody CommentDTO dto) {
        var analysis = analysisService.saveComment(analysisId, dto.comment());
        webSocketService.updateAnalysis(analysis);
    }

    @PostMapping(path = "library/items/{id}")
    public void runFromLibrary(@PathVariable("id") Long itemId, @RequestBody SubmissionRequestDTO submission) {
        var libItem = libraryService.byId(itemId);
        var analysis = analysisService.processSubmissionRequest(submission);
        var dir = analysisService.copyAnalysisFilesFromLibraryItem(analysis, libItem);
        analysisService.sendToEngine(dir, analysis);
    }

    @PostMapping(path = "{id}/rerun")
    public void rerun(@PathVariable("id") Long toRerunId, @RequestBody SubmissionRequestDTO submission) {
        var analysis = analysisService.processSubmissionRequest(submission);
        var dir = analysisService.copyAnalysisFiles(analysis, toRerunId);
        analysisService.sendToEngine(dir, analysis);
    }

    @PostMapping(path = "{id}/cancel")
    public void cancel(@PathVariable("id") Long id) {
        analysisService.cancel(id);
    }

    @GetMapping(path = "{id}/logs")
    public SubmissionResultDTO getLogs(@PathVariable("id") Long id) {
        var analysis = analysisService.byId(id);
        return SubmissionResultDTO.fromAnalysis(analysis, analysis.logs());
    }

    @GetMapping(path = "{id}/results", produces = "application/zip")
    public byte[] downloadResults(@PathVariable("id") Long analysisId, HttpServletResponse response) throws IOException {
        Analysis analysis = analysisService.byId(analysisId);
        var tmpdir = Files.createTempDirectory("tmpDirPrefix").toFile();
        analysisService.writeStdOutToDir(analysis, tmpdir);
        analysisService.writeMetaDataFileToDir(analysis, tmpdir);
        analysisService.resultFilesToDir(analysis, tmpdir);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + analysisId + ".zip\"");
        var result = ZipUtil.zip(tmpdir);
        FileUtils.deleteDirectory(tmpdir);
        return result;
    }


}
