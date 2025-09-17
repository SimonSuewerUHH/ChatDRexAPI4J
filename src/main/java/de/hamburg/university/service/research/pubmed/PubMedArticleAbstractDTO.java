package de.hamburg.university.service.research.pubmed;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PubMedArticleAbstractDTO {

    @XmlElement(name = "AbstractText")
    private List<PubMedArticleAbstractTextDTO> abstractTexts;

}
