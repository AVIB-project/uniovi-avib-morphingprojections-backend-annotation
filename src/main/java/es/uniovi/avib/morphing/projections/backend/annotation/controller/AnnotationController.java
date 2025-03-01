package es.uniovi.avib.morphing.projections.backend.annotation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import es.uniovi.avib.morphing.projections.backend.annotation.service.AnnotationService;
import es.uniovi.avib.morphing.projections.backend.annotation.domain.Annotation;

@Slf4j
@CrossOrigin(maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("annotations")
public class AnnotationController {
	private final AnnotationService annotationService;
	
	@RequestMapping(method = { RequestMethod.GET }, produces = "application/json")
	public ResponseEntity<List<Annotation>> findAll() {
		List<Annotation> annotations = (List<Annotation>) annotationService.findAll();
					
		log.debug("findAll: found {} annotations", annotations.size());
		
		return new ResponseEntity<List<Annotation>>(annotations, HttpStatus.OK);			
	}
	
	@RequestMapping(method = { RequestMethod.GET }, produces = "application/json", value = "/cases/{caseId}")	
	public ResponseEntity<List<Annotation>> findAllByCaseId(@PathVariable String caseId) {
		List<Annotation> annotations = annotationService.findAllByCaseId(caseId);
										
		log.debug("findAllByCaseId: found {} annotations", annotations.size());
			
		return new ResponseEntity<List<Annotation>>(annotations, HttpStatus.OK);		
	}
	
	@RequestMapping(method = { RequestMethod.GET }, produces = "application/json", value = "/cases/{caseId}/available")
	public ResponseEntity<List<Annotation>> findAllAvailableByCaseId(@PathVariable String caseId) {
		List<Annotation> annotations = annotationService.findAllAvailableByCaseId(caseId);
					
		log.debug("findAll: found available {} annotations", annotations.size());
		
		return new ResponseEntity<List<Annotation>>(annotations, HttpStatus.OK);			
	}
	
	@RequestMapping(method = { RequestMethod.GET }, produces = "application/json", value = "/{annotationId}")	
	public ResponseEntity<Annotation> findById(@PathVariable String annotationId) {
		Annotation annotation = annotationService.findById(annotationId);
										
		log.debug("findById: found annotation with annotationId: {}", annotation.getAnnotationId());
			
		return new ResponseEntity<Annotation>(annotation, HttpStatus.OK);		
	}
		
	@RequestMapping(method = { RequestMethod.POST }, produces = "application/json")	
	public ResponseEntity<Annotation> save(@RequestBody Annotation annotation) {		
		Annotation annotationSaved = annotationService.save(annotation);

		log.debug("save: create/update annotation with annotationId: {} from Manager Service", annotationSaved.getAnnotationId());
			
		return new ResponseEntity<Annotation>(annotationSaved, HttpStatus.OK);			
	}

	@RequestMapping(method = { RequestMethod.DELETE }, value = "/{annotationId}")	
	public void deleteById(@PathVariable String annotationId) {
		log.debug("deleteById: remove annotation with annotationId: {}", annotationId);
			
		annotationService.deleteById(annotationId);					
	}

	@RequestMapping(method = { RequestMethod.DELETE }, value = "/cases/{caseId}")	
	public void deleteAllByCaseId(@PathVariable String caseId) {
		log.debug("deleteAllByCaseId: remove all annotation with caseId: {}", caseId);
			
		annotationService.deleteAllByCaseId(caseId);					
	}
	
    @RequestMapping(method = { RequestMethod.POST }, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json", value = "/organizations/{organizationId}/projects/{projectId}/cases/{caseId}")
    public ResponseEntity<List<Annotation>> uploadFiles(
    		@PathVariable String organizationId,
    		@PathVariable String projectId,
    		@PathVariable String caseId,
            @RequestPart("file[]") MultipartFile[] files) {
		log.info("upload files from controller");
		
		List<Annotation> annotations = annotationService.uploadFiles(organizationId, projectId, caseId, files);
													                
		return new ResponseEntity<List<Annotation>>(annotations, HttpStatus.OK);			
	}		
}
