package se.sundsvall.relations.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;

@Data
@Builder(setterPrefix = "with")
@Schema(description = "Paged relation response")
public class RelationPagedResponse {

	@JsonProperty("_meta")
	@Schema(implementation = PagingAndSortingMetaData.class, accessMode = READ_ONLY)
	private PagingAndSortingMetaData metaData;

	@ArraySchema(schema = @Schema(implementation = Relation.class, accessMode = READ_ONLY))
	private List<Relation> relations;
}
