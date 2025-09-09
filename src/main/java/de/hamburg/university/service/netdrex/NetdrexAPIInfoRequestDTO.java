package de.hamburg.university.service.netdrex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetdrexAPIInfoRequestDTO {
    @JsonProperty("collection_name")
    private String collection;

    @JsonProperty("id_list")
    private List<String> ids;
}
