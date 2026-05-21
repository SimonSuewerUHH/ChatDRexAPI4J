package de.hamburg.university.service.research.semanticscholar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class SemanticScholarServiceImpl {

    @Inject
    @RestClient
    SemanticScholarService semanticScholarService;

    public SemanticScholarResponseDTO executeQuery(String query) {
        return semanticScholarService.search(query);
    }
}
