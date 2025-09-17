package de.hamburg.university.service.research.pubmed;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PubMedELocationIDDTO {
    @XmlAttribute(name = "EIdType")
    private String eidType;

    @XmlAttribute(name = "ValidYN")
    private String validYN;

    @XmlValue
    private String value;

}
