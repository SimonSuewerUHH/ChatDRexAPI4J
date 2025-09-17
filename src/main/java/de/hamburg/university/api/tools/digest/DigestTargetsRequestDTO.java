package de.hamburg.university.api.tools.digest;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DigestTargetsRequestDTO {
    @NotEmpty
    private List<String> target;
}
