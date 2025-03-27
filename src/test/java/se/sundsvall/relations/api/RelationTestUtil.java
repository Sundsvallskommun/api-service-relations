package se.sundsvall.relations.api;

import se.sundsvall.relations.api.model.Relation;

public class RelationTestUtil {

	public static Relation createRelationInstance() {
		return Relation.builder()
			.withSourceId("sourceId")
			.withSourceService("sourceService")
			.withSourceNamespace("sourceNamespace")
			.withSourceType("sourceType")
			.withTargetId("targetId")
			.withTargetService("targetService")
			.withTargetNamespace("targetNamespace")
			.withTargetType("targetType")
			.withType("type")
			.build();
	}
}
