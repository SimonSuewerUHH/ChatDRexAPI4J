package de.hamburg.university.agent.tool.digest;

import de.hamburg.university.service.digest.DigestApiClientService;
import de.hamburg.university.service.digest.DigestToolResultDTO;
import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class DigestTools {

    @Inject
    DigestApiClientService digestService;


    @Tool("Run functional enrichment (DIGEST-Set) for human genes by Entrez ID. " +
            "Use when the user says 'set enrichment', 'gene set', or does not specify. " +
            "Input: List of Entrez IDs as strings, e.g. [\"1636\",\"102\"]. " +
            "Output: DigestToolResultDTO with enrichment results."
    )
    public Uni<DigestToolResultDTO> submitSet(List<String> entrez) {
        return digestService.callSet(entrez)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onFailure().invoke(t -> Log.error("DigestSet run failed", t));
    }

    @Tool("Run network-aware enrichment (DIGEST-Subnetwork) for human genes by Entrez ID. " +
            "Use when the user mentions 'subnetwork', 'module', or 'network-based' enrichment. " +
            "Input: List of Entrez IDs as strings, e.g. [\"1636\",\"102\"]. " +
            "Output: DigestToolResultDTO with enrichment results."
    )
    public Uni<DigestToolResultDTO> submitSubnetwork(List<String> entrez) {
        return digestService.callSubnetwork(entrez)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onFailure().invoke(t -> Log.error("DigestSubnetwork run failed", t));
    }
}
