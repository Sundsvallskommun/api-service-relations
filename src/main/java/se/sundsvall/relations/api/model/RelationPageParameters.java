package se.sundsvall.relations.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import se.sundsvall.dept44.models.api.paging.AbstractParameterPagingAndSortingBase;

@Schema(description = "Page parameters")
public class RelationPageParameters extends AbstractParameterPagingAndSortingBase {
}
