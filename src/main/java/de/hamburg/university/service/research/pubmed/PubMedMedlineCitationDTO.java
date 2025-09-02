package de.hamburg.university.service.research.pubmed;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PubMedMedlineCitationDTO {

    @XmlElement(name = "PMID")
    private String pmid;

    @XmlElement(name = "Article")
    private PubMedArticleDataDTO article;
}
