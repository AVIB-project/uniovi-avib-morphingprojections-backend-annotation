package es.uniovi.avib.morphing.projections.backend.annotation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import es.uniovi.avib.morphing.projections.backend.annotation.repository.AnnotationRepository;
import es.uniovi.avib.morphing.projections.backend.annotation.domain.Annotation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnotationService {
	private final  AnnotationRepository annotationRepository;
	
	public List<Annotation> findAll() {		
		log.debug("findAll: found all annotations");
		
		return (List<Annotation>) annotationRepository.findAll();		
	}
	
	public Annotation findById(String annotationId) {
		log.debug("findById: found annotation with id: {}", annotationId);
		
		return annotationRepository.findById(annotationId).orElseThrow(() -> new RuntimeException("Annotation not found"));	
	}
		
	public Annotation save(Annotation annotation) {
		log.debug("save: save annotation");
		
		return annotationRepository.save(annotation);
	}
	
	public void deleteById(String annotationId) {
		log.debug("deleteById: delete annotation with id: {}", annotationId);
		
		annotationRepository.deleteById(annotationId);
	}
}
