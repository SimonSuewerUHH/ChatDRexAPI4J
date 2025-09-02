package de.hamburg.university.service.mygene;

import lombok.Data;

import java.util.List;

@Data
public class MyGeneResponseDTO {
    private List<MyGeneHit> hits;

}
