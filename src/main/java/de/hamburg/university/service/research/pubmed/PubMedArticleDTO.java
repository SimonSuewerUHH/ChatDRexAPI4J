package de.hamburg.university.service.research.pubmed;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PubMedArticleDTO {

    @XmlElement(name = "MedlineCitation")
    private PubMedMedlineCitationDTO medlineCitation;

    @XmlElement(name = "PubmedData")
    private PubMedHistoryWrapperDTO pubmedData;
}
