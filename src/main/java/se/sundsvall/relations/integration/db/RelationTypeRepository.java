package se.sundsvall.relations.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.sundsvall.relations.integration.db.model.RelationTypeEntity;

@CircuitBreaker(name = "relationTypeRepository")
public interface RelationTypeRepository extends JpaRepository<RelationTypeEntity, String> {

	Optional<RelationTypeEntity> findByType(String type);

	boolean existsByType(String type);

	boolean existsByCounterType(String counterType);

	@Query("SELECT COUNT(rt) > 0 FROM RelationTypeEntity rt WHERE rt.type = :value OR rt.counterType = :value")
	boolean existsByTypeOrCounterType(@Param("value") String value);

	void deleteByType(String type);
}
