package de.hamburg.university.service.digest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigestToolPlotDTO {
    private List<DigestToolPlotEntryDTO> entries;
}
