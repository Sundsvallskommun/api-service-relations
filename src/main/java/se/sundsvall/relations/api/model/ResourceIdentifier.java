package se.sundsvall.relations.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
@Schema(description = "Resource identifier for source and target")
public class ResourceIdentifier {

	// Source
	@Schema(description = "Unique id for the object", examples = "some-id")
	@NotBlank
	private String resourceId;

	@Schema(description = "Type of object", examples = "case")
	@NotBlank
	private String type;

	@Schema(description = "Name of service where object exists")
	@NotBlank
	private String service;

	@Schema(description = "Namespace of object")
	private String namespace;
}
