package de.hamburg.university.agent.tool.netdrex;

import de.hamburg.university.service.netdrex.diamond.DiamondResultsDTO;
import de.hamburg.university.service.netdrex.diamond.DiamondToolClientService;
import de.hamburg.university.service.netdrex.diamond.SeedPayloadDTO;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static io.quarkus.arc.ComponentsProvider.LOG;

@ApplicationScoped
public class DiamondTool {

    @Inject
    DiamondToolClientService diamondToolService;

    public Uni<DiamondResultsDTO> run(List<Integer> entrezIds) {
        return Uni.createFrom().item(() -> entrezIds.stream()
                        .map(String::valueOf)
                        .toList()
                )
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .flatMap(ids -> diamondToolService.run(new SeedPayloadDTO(ids)))
                .onFailure().invoke(e -> LOG.error("Error at Diamond-Tool", e))
                .onFailure().transform(e -> new RuntimeException("Error at Diamond-Tool: " + e.getMessage(), e));
    }


    public List<String> parseGenes(String query) {
        String regex = "(?i)and|,|;";
        String[] parts = query.split(regex);
        List<String> genes = new ArrayList<>();
        for (String part : parts) {
            String gene = part.trim();
            if (!gene.isEmpty()) {
                genes.add(gene);
            }
        }
        return genes;
    }


}
