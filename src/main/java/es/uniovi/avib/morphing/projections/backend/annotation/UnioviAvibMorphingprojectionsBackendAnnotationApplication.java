package es.uniovi.avib.morphing.projections.backend.annotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class UnioviAvibMorphingprojectionsBackendAnnotationApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnioviAvibMorphingprojectionsBackendAnnotationApplication.class, args);
	}
	
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
