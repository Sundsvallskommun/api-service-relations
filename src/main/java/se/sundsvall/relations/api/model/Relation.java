package se.sundsvall.relations.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Builder(setterPrefix = "with")
@Schema(description = "Relation between objects")
public class Relation {

	// Relation
	@Schema(description = "Unique id for the relation", examples = "f4de6b8b-f727-4ed1-9959-b9d5bde1922f", accessMode = READ_ONLY)
	@Null
	private String id;

	@Schema(description = "Type of relation. Valid types can be fetch via /relation-types")
	@NotBlank
	private String type;

	@Schema(description = "Timestamp when relations was created", examples = "2000-10-31T01:30:00.000+02:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	@Null
	private OffsetDateTime created;

	@Schema(description = "Timestamp when relations was last modified", examples = "2000-10-31T01:30:00.000+02:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	@Null
	private OffsetDateTime modified;

	@Schema(description = "Source identifiers")
	@NotNull
	private ResourceIdentifier source;

	@Schema(description = "Target identifiers")
	@NotNull
	private ResourceIdentifier target;
}
