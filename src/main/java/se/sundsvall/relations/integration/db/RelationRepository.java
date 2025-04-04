package se.sundsvall.relations.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import se.sundsvall.relations.integration.db.model.RelationEntity;

@CircuitBreaker(name = "relationRepository")
public interface RelationRepository extends JpaRepository<RelationEntity, String>, JpaSpecificationExecutor<RelationEntity> {

	Optional<RelationEntity> findByIdAndMunicipalityId(String id, String municipalityId);
}
