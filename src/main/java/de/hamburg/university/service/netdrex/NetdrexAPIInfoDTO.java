package de.hamburg.university.service.netdrex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetdrexAPIInfoDTO {
    private String primaryDomainId;
    private String casNumber;
    private String created;
    private List<String> dataSources;
    private String description;
    private String displayName;
    private List<String> domainIds;
    private List<String> drugCategories;
    private List<String> drugGroups;
    private String indication;
    private List<String> synonyms;
    private String type;
    private String updated;
}