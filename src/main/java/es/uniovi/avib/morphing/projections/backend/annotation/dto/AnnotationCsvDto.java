package es.uniovi.avib.morphing.projections.backend.annotation.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({ "name", "description", "group", "type",  "colorized",  "mandatory",  "required", "label", "values" })
public class AnnotationCsvDto {
	private String name;
	private String description;
	private String group;
	private String type;
	private boolean colorized;
	private boolean mandatory;		
	private boolean required;	
	private String label;	
	private String values;	
}
