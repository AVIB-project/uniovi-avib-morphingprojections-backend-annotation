package es.uniovi.avib.morphing.projections.backend.annotation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
					
		log.debug("findAll: found {} recommendations", annotations.size());
		
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

	@RequestMapping(method = { RequestMethod.DELETE },value = "/{annotationId}")	
	public void deleteById(@PathVariable String annotationId) {
		log.debug("deleteById: remove annotation with annotationId: {}", annotationId);
			
		annotationService.deleteById(annotationId);					
	}
}
