package se.sundsvall.relations.api;

import se.sundsvall.relations.api.model.Relation;
import se.sundsvall.relations.api.model.ResourceIdentifier;

public final class RelationTestUtil {

	private RelationTestUtil() {}

	public static Relation createRelationInstance() {
		return Relation.builder()
			.withSource(ResourceIdentifier.builder()
				.withResourceId("sourceId")
				.withService("sourceService")
				.withNamespace("sourceNamespace")
				.withType("sourceType")
				.build())
			.withTarget(ResourceIdentifier.builder()
				.withResourceId("targetId")
				.withService("targetService")
				.withNamespace("targetNamespace")
				.withType("targetType")
				.build())
			.withType("type")
			.build();
	}
}
