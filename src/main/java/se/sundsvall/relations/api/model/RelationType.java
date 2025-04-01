package se.sundsvall.relations.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
@Schema(description = "Type of relation")
public class RelationType {

	@Schema(description = "Name of type", example = "DUPLICATES")
	@NotBlank
	private String type;

	@Schema(description = "Inverse value of type (if applicable)", example = "IS DUPLICATED BY")
	private String counterType;
}
