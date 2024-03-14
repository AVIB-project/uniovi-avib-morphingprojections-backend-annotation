package es.uniovi.avib.morphing.projections.backend.annotation.domain;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "annotation")
public class Annotation {
	@Id	
	private String annotationId;
	
	@NotNull(message = "Case Id may not be null")
	@Field("case_id")
	private ObjectId caseId;
	
	@NotNull(message = "Name may not be null")
	@Field("name")
	private String name;
	
	@NotNull(message = "Description may not be null")
	@Field("description")
	private String description;
	
	@NotNull(message = "Label may not be null")
	@Field("label")
	private Map<String, String> label;	
	
	@Field("values")
	private List<String> values;
	
	@NotNull(message = "Group may not be null")
	@Field("group")
	private String group;
	
	@Field("space")
	private String space;
	
	@Field("precalculated")
	private boolean precalculated = false;
	
	@Field("projected_by_annotation")
	private String projectedByAnnotation;
	
	@Field("projection")
	private String projection;
	
	@NotNull(message = "Encoding may not be null")
	@Field("encoding")
	private String encoding;
	
	@Field("encoding_name")
	private String encodingName;	
		
	@NotNull(message = "Type may not be null")
	@Field("type")
	private String type;
	
	@NotNull(message = "Colorized may not be null")
	@Field("colorized")
	private boolean colorized = false;
	
	@NotNull(message = "Required may not be null")
	@Field("required")
	private boolean required = true;
	
	@NotNull(message = "Creation Date may not be null")
	@Field("creation_date")
	private Date creationDate;	
	
	@NotNull(message = "Creation by may not be null")
	@Field("creation_by")
	private String creationBy;	
	
	@Field("updated_date")
	private Date updatedDate;	
	
	@Field("updated_by")
	private String updatedBy;		
}
