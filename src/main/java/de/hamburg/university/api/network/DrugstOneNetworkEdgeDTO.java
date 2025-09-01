package de.hamburg.university.api.network;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugstOneNetworkEdgeDTO {
    @NotBlank
    private String from;

    @NotBlank
    private String to;

    private String group = "default";
}
