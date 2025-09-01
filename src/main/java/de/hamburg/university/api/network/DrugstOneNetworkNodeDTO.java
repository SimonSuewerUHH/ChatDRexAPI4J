package de.hamburg.university.api.network;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugstOneNetworkNodeDTO {
    @NotBlank
    private String id;

    @NotBlank
    private String group;

    private String label;
}
