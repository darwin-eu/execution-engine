package eu.darwin.node.repo;


import eu.darwin.node.domain.AnalysisFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisFileRepo extends JpaRepository<AnalysisFile, Long> {

    List<AnalysisFile> findAllByAnalysisIdAndType(Long analysisId, AnalysisFile.Type type);
}
