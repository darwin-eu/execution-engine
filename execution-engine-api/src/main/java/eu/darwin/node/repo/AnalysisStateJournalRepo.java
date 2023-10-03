package eu.darwin.node.repo;

import eu.darwin.node.domain.Analysis;
import eu.darwin.node.domain.AnalysisStateEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisStateJournalRepo extends JpaRepository<AnalysisStateEntry, Long> {

    List<AnalysisStateEntry> findAllByAnalysis(Analysis analysis);

}
