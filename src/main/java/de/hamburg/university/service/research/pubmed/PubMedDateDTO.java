package de.hamburg.university.service.research.pubmed;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PubMedDateDTO {

    @XmlAttribute(name = "PubStatus")
    private String pubStatus;

    @XmlElement(name = "Year")
    private String year;

    @XmlElement(name = "Month")
    private String month;

    @XmlElement(name = "Day")
    private String day;

}
