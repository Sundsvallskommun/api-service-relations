package se.sundsvall.relations.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Builder(setterPrefix = "with")
@Schema(description = "Relation between objects")
public class Relation {
	// TODO verify RequiredMode (defaults to AUTO)

	// Relation
	@Schema(description = "Unique id for the relation", example = "f4de6b8b-f727-4ed1-9959-b9d5bde1922f", accessMode = READ_ONLY)
	@Null
	private String id;

	@Schema(description = "Type of relation. Valid types can be fetch via /relation-types")
	@NotBlank
	private String type;

	@Schema(description = "Timestamp when relations was created", example = "2000-10-31T01:30:00.000+02:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	@Null
	private OffsetDateTime created;

	@Schema(description = "Timestamp when relations was last modified", example = "2000-10-31T01:30:00.000+02:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	@Null
	private OffsetDateTime modified;

	// Source
	@Schema(description = "Unique id for the source object", example = "some-id")
	@NotBlank
	private String sourceId;

	@Schema(description = "Type of source object")
	@NotBlank
	private String sourceType;

	@Schema(description = "Name of RelationService where source object exists")
	@NotBlank
	private String sourceService;

	@Schema(description = "Namespace of source object")
	private String sourceNamespace;

	// Target
	@Schema(description = "Unique id for the target object", example = "some-id")
	@NotBlank
	private String targetId;

	@Schema(description = "Type of target object")
	@NotBlank
	private String targetType;

	@Schema(description = "Name of RelationService where target object exists")
	@NotBlank
	private String targetService;

	@Schema(description = "Namespace of target object")
	private String targetNamespace;
}
