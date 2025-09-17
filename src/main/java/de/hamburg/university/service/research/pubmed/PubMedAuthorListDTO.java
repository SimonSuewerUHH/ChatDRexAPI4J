package de.hamburg.university.service.research.pubmed;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PubMedAuthorListDTO {

    @XmlAttribute(name = "CompleteYN")
    private String completeYN;

    @XmlElement(name = "Author")
    private List<PubMedAuthorDTO> authors;

}
