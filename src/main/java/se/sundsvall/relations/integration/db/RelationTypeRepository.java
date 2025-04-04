package se.sundsvall.relations.integration.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.relations.integration.db.model.RelationTypeEntity;

public interface RelationTypeRepository extends JpaRepository<RelationTypeEntity, String> {

	Optional<RelationTypeEntity> findByType(String type);

	boolean existsByType(String Type);

	boolean existsByCounterType(String counterType);

	void deleteByType(String type);
}
