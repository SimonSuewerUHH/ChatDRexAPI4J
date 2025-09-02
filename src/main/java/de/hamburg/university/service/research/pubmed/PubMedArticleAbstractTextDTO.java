package de.hamburg.university.service.research.pubmed;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PubMedArticleAbstractTextDTO {

    @XmlAttribute(name = "Label")
    private String label;

    @XmlAttribute(name = "NlmCategory")
    private String nlmCategory;

    @XmlValue
    private String text;
}
