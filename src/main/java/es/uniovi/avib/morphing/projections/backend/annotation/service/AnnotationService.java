package es.uniovi.avib.morphing.projections.backend.annotation.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import es.uniovi.avib.morphing.projections.backend.annotation.repository.AnnotationRepository;
import es.uniovi.avib.morphing.projections.backend.annotation.domain.Annotation;
import es.uniovi.avib.morphing.projections.backend.annotation.dto.AnnotationCsvDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnotationService {	
	private final AnnotationRepository annotationRepository;
	private final MongoTemplate mongoTemplate;
	
	@SuppressWarnings("serial")
	private Annotation createEncodingAnnotation(Annotation annotation, String projection) {
		Annotation annotationEncoding = new Annotation();
		
		annotationEncoding.setCaseId(annotation.getCaseId());
		annotationEncoding.setGroup("encoding");
		annotationEncoding.setType("numeric");
		annotationEncoding.setName(projection + "_" + annotation.getName());
		annotationEncoding.setLabel(new HashMap<String, String>() {{
		    put("us", projection.toUpperCase() + " " + annotation.getName());
		    put("es", projection.toUpperCase() + " " + annotation.getName());
		}});
		annotationEncoding.setDescription(projection.toUpperCase() + " " + annotation.getName() + " encoding projection");
		annotationEncoding.setEncodingName(annotation.getName());
		annotationEncoding.setColorized(false);
		annotationEncoding.setRequired(true);
		
		return annotationEncoding;
	}
	
	public List<Annotation> findAll() {
		log.debug("findAll: found all annotations");
		
		return annotationRepository.findAll();		
	}
	
	public List<Annotation> findAllByCaseId(String caseId) {
		log.debug("findAllByCaseId: find annotations by case id: {}", caseId);
		
		AggregationOperation aggregationOperation = Aggregation
				.match(Criteria.where("case_id").is(new ObjectId(caseId)));
			
		Aggregation aggregation = Aggregation.newAggregation(aggregationOperation);
		
		List<Annotation> annotations = mongoTemplate.aggregate(aggregation, "annotation", Annotation.class).getMappedResults();					
									
		return annotations;
	}
	
	public List<Annotation> findAllAvailableByCaseId(String caseId) {
		log.debug("findAllAvailable: find annotations");
				
		AggregationOperation aggregationMatchOperation = Aggregation
				.match(Criteria.where("group").ne("encoding")
						.andOperator(Criteria.where("case_id").is(new ObjectId(caseId))));
		
		AggregationOperation aggregationProjectOperation = Aggregation
				.project("_id", "name", "description", "group", "type", "values", "space", "encoding", "encoding_name", "label", "projection", "projected_by_annotation", "projected_by_annotation_value", "precalculated", "colorized", "required");
							    
		Aggregation aggregation = Aggregation.newAggregation(aggregationMatchOperation, aggregationProjectOperation);
		
		List<Annotation> annotations = mongoTemplate.aggregate(aggregation, "annotation", Annotation.class).getMappedResults();					
									
		return annotations;
	}
	
	public List<Annotation> findAllByEncodingName(String encodingName) {
		log.debug("findAllByEncodingName: find annotations by encoding name: {}", encodingName);
		
		AggregationOperation aggregationOperation = Aggregation
				.match(Criteria.where("encoding_name").is(encodingName));
			
		Aggregation aggregation = Aggregation.newAggregation(aggregationOperation);
		
		List<Annotation> annotations = mongoTemplate.aggregate(aggregation, "annotation", Annotation.class).getMappedResults();					
									
		return annotations;
	}
	
	public Annotation findById(String annotationId) {
		log.debug("findById: found annotation with id: {}", annotationId);
		
		return annotationRepository.findById(annotationId).orElseThrow(() -> new RuntimeException("Annotation not found"));	
	}
	
	public Annotation findByName(String name) {
		log.debug("findByName: find annotation by encoding name: {}", name);
		
		AggregationOperation aggregationOperation = Aggregation
				.match(Criteria.where("name").is(name));
			
		Aggregation aggregation = Aggregation.newAggregation(aggregationOperation);
		
		List<Annotation> annotations = mongoTemplate.aggregate(aggregation, "annotation", Annotation.class).getMappedResults();					
									
		return annotations.get(0);
	}
		
	public Annotation save(Annotation annotation) {		
		if (annotation.getGroup().equals("projection")) {
			// save X encoding annotation		
			annotationRepository.save(createEncodingAnnotation(annotation, "x"));
			
			// create Y encoding annotations						
			annotationRepository.save(createEncodingAnnotation(annotation, "y"));		
		}

		// save principal annotation
		return annotationRepository.save(annotation);
	}
	
	public List<Annotation> saveAll(List<Annotation> annotations) {
		log.debug("save: save annotation");
		
		return annotationRepository.saveAll(annotations);
	}
	
	public void deleteById(String annotationId) {
		log.debug("deleteById: delete annotation with id: {}", annotationId);
		
		// get annotation
		Annotation annotation = findById(annotationId);
		
		// get encoding annotations
		List<Annotation> encodedAnnotations = findAllByEncodingName(annotation.getName());
		
		// remove annotation
		annotationRepository.delete(annotation);
		
		// remove encoding annotations
		annotationRepository.deleteAll(encodedAnnotations);
	}
	
	public void deleteAllByCaseId(String caseId) {
		log.debug("deleteAllByCaseId: delete annotations with case id: {}", caseId);
		
		// get all annotations by case id
		List<Annotation> annotations = findAllByCaseId(caseId);
		
		// remove all annotations
		annotationRepository.deleteAll(annotations);
	}
		
	public List<Annotation> uploadFiles(String organizationId, String projectId, String caseId, MultipartFile[] files) {
		log.info("update files from service");
						
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
				
		List<Annotation> annotations = new ArrayList<Annotation>();
		
		Arrays.asList(files).forEach(file -> {	
			// configure csv mapper
	        CsvMapper csvMapper = new CsvMapper();
	        CsvSchema csvSchema = CsvSchema.builder()
	        		.setSkipFirstDataRow(true)
	        		.addColumn("group", CsvSchema.ColumnType.STRING)	        		
	        		.addColumn("name", CsvSchema.ColumnType.STRING)
	        		.addColumn("label", CsvSchema.ColumnType.STRING)	        		
	        		.addColumn("description", CsvSchema.ColumnType.STRING)
	        		.addColumn("space", CsvSchema.ColumnType.STRING)
	        		.addColumn("precalculated", CsvSchema.ColumnType.BOOLEAN)
	        		.addColumn("projected_by_annotation", CsvSchema.ColumnType.STRING)
	        		.addColumn("projection", CsvSchema.ColumnType.STRING)
	        		.addColumn("type", CsvSchema.ColumnType.STRING)
	        		.addColumn("colorized", CsvSchema.ColumnType.BOOLEAN)
	        		.addColumn("required", CsvSchema.ColumnType.BOOLEAN)
	        		.addColumn("values", CsvSchema.ColumnType.STRING)	        		  
	        		.build();
	        
	        try (MappingIterator<AnnotationCsvDto> mappingIterator = csvMapper
	        		.readerFor(AnnotationCsvDto.class)
	        		.with(csvSchema)
	        		.readValues(file.getInputStream())) {
	        	while (mappingIterator.hasNextValue()) {
	        		AnnotationCsvDto annotationCsvDto = mappingIterator.nextValue();
	        		
	        		// create a new annotation for each csv row configuration	        		
	        		Annotation annotation = new Annotation();
	        		annotation.setCaseId(caseId);
	        		annotation.setGroup(annotationCsvDto.getGroup());
	        		annotation.setName(annotationCsvDto.getName());
	        		annotation.setDescription(annotationCsvDto.getDescription());
	        		if (!annotationCsvDto.getSpace().isEmpty())
	        			annotation.setSpace(annotationCsvDto.getSpace());
	        		annotation.setPrecalculated(annotationCsvDto.isPrecalculated());
	        		if (!annotationCsvDto.getProjected_by_annotation().isEmpty())
	        			annotation.setProjectedByAnnotation(annotationCsvDto.getProjected_by_annotation());
	        		annotation.setType(annotationCsvDto.getType());
	        		annotation.setColorized(annotationCsvDto.isColorized());
	        		annotation.setRequired(annotationCsvDto.isRequired());
	        		
	        		if (annotation.getGroup().equals("projection")) {
	        			annotation.setEncoding("supervised");	        			
	        		}
	        		
	        		if (!annotation.isPrecalculated()) {
	        			annotation.setProjection("tsne");
	        		}
	        		
	        		// parse labels for all annotations
	        		annotation.setLabel(new HashMap<String, String>());
	        		for(String labelItem : annotationCsvDto.getLabel().split("\\|")) {
	        			String[] labelValues = labelItem.split("\\#");
	        				
	        			annotation.getLabel().put(labelValues[0], labelValues[1]);
	        		}
	        		
	        		// insert all new annotations
	        		annotations.add(annotation);
	        		
	        		// parse values for enumeration annotations
	        		if (annotationCsvDto.getValues() != "") {
	        			annotation.setValues(new ArrayList<String>());
	        		
		        		for(String valueItem : annotationCsvDto.getValues().split("\\|")) {
		        			annotation.getValues().add(valueItem);
		        		}
	        		}
	        		
	        		// add X/Y encoding annotations for projection annotation
	        		if (annotation.getGroup().equals("projection")) {
	        			annotations.add(createEncodingAnnotation(annotation, "x"));
	        			annotations.add(createEncodingAnnotation(annotation, "y"));
	        		}	        	
	        	}	        		            
	        } catch (IOException e) {
				e.printStackTrace();								
			}		       
		});	
		
		// remove all old annotations
		deleteAllByCaseId(caseId);
		
		return saveAll(annotations);
	}
}
