package es.uniovi.avib.morphing.projections.backend.annotation.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({ "group", "name", "label", "description", "space", "precalculated", "projected_by_annotation", "projection", "type",  "colorized",  "required", "values" })
public class AnnotationCsvDto {
	private String group;
	private String name;
	private String label;	
	private String description;
	private String space;
	private boolean precalculated;
	private String projected_by_annotation;
	private String projection;
	private String type;
	private boolean colorized;	
	private boolean required;	
	private String values;	
}	
