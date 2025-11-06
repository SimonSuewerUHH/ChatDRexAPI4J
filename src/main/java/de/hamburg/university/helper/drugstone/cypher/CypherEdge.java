package de.hamburg.university.helper.drugstone.cypher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CypherEdge {
    private String n1PrimaryDomainId;

    private String n1Type;

    private String n1DisplayName;

    private String relationType;

    private String n2PrimaryDomainId;

    private String n2Type;

    private String n2DisplayName;


    public String getN2Id(){
        return removePrefix(n2PrimaryDomainId);
    }

    public String getN1Id(){
        return removePrefix(n1PrimaryDomainId);
    }
    private String removePrefix(String raw) {
        if (raw != null && raw.contains(".")) {
            String[] parts = raw.split("\\.");
            if (parts.length > 1) {
                return parts[1];
            }
        }
        return raw;
    }
}
