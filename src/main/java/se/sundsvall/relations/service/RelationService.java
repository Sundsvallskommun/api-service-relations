package se.sundsvall.relations.service;

import jakarta.persistence.Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import se.sundsvall.relations.api.model.Relation;

@Service
public class RelationService {

	public String createRelation(String municipalityId, Relation relation) {
		// TODO: implement
		return null;
	}

	public Page<Relation> findRelations(String municipalityId, Specification<? extends Entity> filter, Pageable pageable) {
		// TODO: implement
		return null;
	}

	public Relation saveRelation(String municipalityId, Relation relation) {
		// TODO: implement
		return null;
	}

	public void deleteRelation(String municipalityId, String id) {
		// TODO: implement
	}

	public Relation getRelation(String municipalityId, String id) {
		// TODO: implement
		return null;
	}
}
