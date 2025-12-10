package se.sundsvall.relations.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
@Schema(description = "Type of relation")
public class RelationType {

	@Schema(description = "Name of type", examples = "DUPLICATES")
	@NotBlank
	private String name;

	@Schema(description = "Display value", examples = "Duplicates")
	private String displayName;

	@Schema(description = "Inverse value of type (if applicable)", examples = "IS DUPLICATED BY")
	private String counterName;

	@Schema(description = "Display value", examples = "Is duplicated by")
	private String counterDisplayName;
}
