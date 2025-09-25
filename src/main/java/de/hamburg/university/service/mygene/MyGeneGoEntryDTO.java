package de.hamburg.university.service.mygene;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyGeneGoEntryDTO {
    private Map<String, List<MyGeneGoTermDTO>> categories = new HashMap<>();

    @JsonAnySetter
    public void addCategory(String key, Object value) {
        if (value instanceof Map) {
            MyGeneGoTermDTO term = mapToDto((Map<?, ?>) value);
            categories.put(key, Collections.singletonList(term));
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<MyGeneGoTermDTO> terms = new ArrayList<>();
            for (Object obj : list) {
                if (obj instanceof Map) {
                    terms.add(mapToDto((Map<?, ?>) obj));
                }
            }
            categories.put(key, terms);
        }
    }

    private MyGeneGoTermDTO mapToDto(Map<?, ?> map) {
        MyGeneGoTermDTO dto = new MyGeneGoTermDTO();
        dto.setId((String) map.get("id"));
        dto.setTerm((String) map.get("term"));
        dto.setEvidence((String) map.get("evidence"));

        Object pubmed = map.get("pubmed");
        if (pubmed instanceof String) {
            dto.setPubmed(Collections.singletonList((String) pubmed));
        } else if (pubmed instanceof List) {
            dto.setPubmed(((List<?>) pubmed).stream()
                    .map(Object::toString)
                    .toList());
        }

        return dto;
    }
}
