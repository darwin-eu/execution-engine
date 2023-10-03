package eu.darwin.node.service;

import eu.darwin.node.domain.Analysis;
import eu.darwin.node.domain.Log;
import eu.darwin.node.repo.LogRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogService {

    private final LogRepo repo;
    private final WebSocketService webSocketService;


    public void info(String line, Analysis analysis) {
        log.info(line);
        writeLogLine(line, analysis, "INFO");
    }

    public void error(String line, Analysis analysis) {
        log.error(line);
        writeLogLine(line, analysis, "ERROR");
    }

    private void writeLogLine(String line, Analysis analysis, String level) {
        var logLine = new Log(line, analysis, level);
        repo.saveAndFlush(logLine);
        analysis.addLog(logLine);
        webSocketService.updateAnalysis(analysis);
    }


}

