package de.hamburg.university.service.research.pubmed;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class PubmedServiceImpl {

    @Inject
    @RestClient
    PubmedService pubmedService;


    public PubMedSearchResultDTO executeQuery(String query) {
        return pubmedService.search(query);
    }

}
