package es.uniovi.avib.morphing.projections.backend.annotation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import es.uniovi.avib.morphing.projections.backend.annotation.domain.Annotation;

@Repository
public interface AnnotationRepository extends MongoRepository<Annotation, String> {
}
