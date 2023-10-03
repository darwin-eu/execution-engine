package eu.darwin.node.service;

import eu.darwin.node.domain.Analysis;
import eu.darwin.node.domain.AnalysisState;
import eu.darwin.node.domain.AnalysisStateEntry;
import eu.darwin.node.domain.User;
import eu.darwin.node.repo.AnalysisStateJournalRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

import static eu.darwin.node.domain.AnalysisState.CANCELLED;
import static eu.darwin.node.domain.AnalysisState.EXECUTED;
import static eu.darwin.node.domain.AnalysisState.FAILED;

@Service
@RequiredArgsConstructor
public class AnalysisStateService {

    private final AnalysisStateJournalRepo repo;
    private final LogService logService;


    public void created(Analysis analysis) {
        var message = String.format("Received execution request for study: [%s] on datasource: [%s] in env: [%s]", analysis.studyId(), analysis.dataSource().name(), analysis.executionEnvironment());
        update(analysis, AnalysisState.CREATED, message);
    }

    public void executing(Analysis analysis) {
        var message = String.format("Started processing request [%s] for study: [%s] on datasource: [%s] in env: [%s]", analysis.id(), analysis.studyId(), analysis.dataSource().name(), analysis.executionEnvironment());
        update(analysis, AnalysisState.EXECUTING, message);
    }

    public void failed(Analysis analysis, String reason) {
        update(analysis, FAILED, reason);
    }

    public void cancelled(Analysis analysis, User user) {
        var username = user == null ? User.anonymous().username() : user.username();
        update(analysis, CANCELLED, "Execution cancelled by " + username);
    }


    public void update(Analysis analysis, AnalysisState state, String reason) {
        // Container may have been cancelled by the user causing it to 'fail', we want to preserve the cancelled status
        var hasBeenCancelled = repo.findAllByAnalysis(analysis).stream().anyMatch(a -> a.state().equals(CANCELLED));
        if (hasBeenCancelled) {
            state = CANCELLED;
        }
        var entry = new AnalysisStateEntry(new Date(), state, reason, analysis);
        analysis.stateHistory().add(entry);
        analysis.status(state);
        if (state.equals(FAILED) || state.equals(EXECUTED) || state.equals(CANCELLED)) {
            analysis.finished(new Date());
        }
        repo.save(entry);
        if (state.equals(FAILED) || state.equals(CANCELLED)) {
            logService.error(reason, analysis);
        } else {
            logService.info(reason, analysis);
        }
    }

}
