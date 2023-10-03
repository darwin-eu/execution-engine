package eu.darwin.node.service;

import eu.darwin.node.domain.Analysis;
import eu.darwin.node.dto.SubmissionResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebSocketService {


    private final SimpMessagingTemplate template;

    public WebSocketService(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void updateAnalysis(Analysis analysis) {
        var dto = SubmissionResultDTO.fromAnalysis(analysis, analysis.logs()); // Logs are lazy loaded, make explicit call
        log.debug("[{}] Submitting analysis update to front-end, current status is: [{}]", dto.id(), dto.status());
        template.convertAndSend("/topic/submissions", dto);
    }

}
