package de.hamburg.university.service.nedrex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NeDRexAPIInfoRequestDTO {
    @JsonProperty("collection_name")
    private String collection;

    @JsonProperty("id_list")
    private List<String> ids;
}
