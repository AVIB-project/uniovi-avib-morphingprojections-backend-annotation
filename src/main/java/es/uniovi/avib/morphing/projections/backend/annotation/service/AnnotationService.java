package es.uniovi.avib.morphing.projections.backend.annotation.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import es.uniovi.avib.morphing.projections.backend.annotation.repository.AnnotationRepository;
import es.uniovi.avib.morphing.projections.backend.annotation.domain.Annotation;
import es.uniovi.avib.morphing.projections.backend.annotation.dto.AnnotationCsvDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnotationService {	
	private final AnnotationRepository annotationRepository;
	private final MongoTemplate mongoTemplate;
	
	public List<Annotation> findAll() {		
		log.debug("findAll: found all annotations");
		
		return (List<Annotation>) annotationRepository.findAll();		
	}

	public List<Annotation> findAllByCaseId(String caseId) {
		log.debug("finfByCaseId: find annotations by case id: {}", caseId);
		
		AggregationOperation aggregationOperation = Aggregation
				.match(Criteria.where("case_id").is(new ObjectId(caseId)));
			
		Aggregation aggregation = Aggregation.newAggregation(aggregationOperation);
		
		List<Annotation> annotations = mongoTemplate.aggregate(aggregation, "annotation", Annotation.class).getMappedResults();					
									
		return annotations;
	}
	
	public Annotation findById(String annotationId) {
		log.debug("findById: found annotation with id: {}", annotationId);
		
		return annotationRepository.findById(annotationId).orElseThrow(() -> new RuntimeException("Annotation not found"));	
	}
	
	public Annotation save(Annotation annotation) {
		log.debug("save: save annotation");
		
		return annotationRepository.save(annotation);
	}
	
	public List<Annotation> saveAll(List<Annotation> annotations) {
		log.debug("save: save annotation");
		
		return annotationRepository.saveAll(annotations);
	}
	
	public void deleteById(String annotationId) {
		log.debug("deleteById: delete annotation with id: {}", annotationId);
		
		annotationRepository.deleteById(annotationId);
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
	        		.addColumn("name", CsvSchema.ColumnType.STRING)
	        		.addColumn("description", CsvSchema.ColumnType.STRING)
	        		.addColumn("group", CsvSchema.ColumnType.STRING)
	        		.addColumn("type", CsvSchema.ColumnType.STRING)
	        		.addColumn("colorized", CsvSchema.ColumnType.BOOLEAN)
	        		.addColumn("mandatory", CsvSchema.ColumnType.BOOLEAN)
	        		.addColumn("required", CsvSchema.ColumnType.BOOLEAN)
	        		.addColumn("label", CsvSchema.ColumnType.STRING)
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
	        		annotation.setCaseId(new ObjectId(caseId));
	        		annotation.setName(annotationCsvDto.getName());
	        		annotation.setDescription(annotationCsvDto.getDescription());
	        		annotation.setGroup(annotationCsvDto.getGroup());
	        		annotation.setType(annotationCsvDto.getType());
	        		annotation.setColorized(annotationCsvDto.isColorized());
	        		annotation.setMandatory(annotationCsvDto.isMandatory());
	        		annotation.setRequired(annotationCsvDto.isRequired());
	        		annotation.setCreationBy("Administrator");
	        		annotation.setCreationDate(new Date());
	        		
	        		// parse labels for all annotations
	        		annotation.setLabel(new HashMap<String, String>());
	        		for(String labelItem : annotationCsvDto.getLabel().split("\\|")) {
	        			String[] labelValues = labelItem.split("\\#");
	        				
	        			annotation.getLabel().put(labelValues[0], labelValues[1]);
	        		}
	        		
	        		// parse values for enumeration annotations
	        		if (annotationCsvDto.getValues() != "") {
	        			annotation.setValues(new ArrayList<String>());
	        		
		        		for(String valueItem : annotationCsvDto.getValues().split("\\|")) {
		        			annotation.getValues().add(valueItem);
		        		}
	        		}
	        		
	        		// remove all old annotations
	        		deleteAllByCaseId(caseId);
	        		
	        		// insert all new annotations
	        		annotations.add(annotation);
	        	}	        		            
	        } catch (IOException e) {
				e.printStackTrace();								
			}		       
		});	
				
		return saveAll(annotations);
	}
}
