package se.sundsvall.relations.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.relations.integration.db.model.RelationTypeEntity;

@CircuitBreaker(name = "relationTypeRepository")
public interface RelationTypeRepository extends JpaRepository<RelationTypeEntity, String> {

	Optional<RelationTypeEntity> findByName(String name);

	boolean existsByName(String name);

	void deleteByName(String name);
}
