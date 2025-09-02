package de.hamburg.university.service.netdrex.diamond;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SeedPayloadDTO {
    private List<String> seeds;
    private int n;
    private int alpha;
    private String network;
    private String edges;

    public SeedPayloadDTO(List<String> seeds) {
        this.seeds = seeds;
        this.setN(100);
        this.setAlpha(1);
        this.setNetwork("DEFAULT");
        this.setEdges("all");
    }
}
