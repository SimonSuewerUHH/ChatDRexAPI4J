package de.hamburg.university.service.research.pubmed;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PubMedAuthorDTO {
    @XmlAttribute(name = "ValidYN")
    private String validYN;

    @XmlElement(name = "LastName")
    private String lastName;

    @XmlElement(name = "ForeName")
    private String foreName;

    @XmlElement(name = "Initials")
    private String initials;

}
