package de.hamburg.university.api.network;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugstOneNetworkDTO {
    @NotNull
    private List<DrugstOneNetworkNodeDTO> nodes = new ArrayList<>();

    @NotNull
    private List<DrugstOneNetworkEdgeDTO> edges = new ArrayList<>();

    @NotBlank
    private String networkType = "default";
}
