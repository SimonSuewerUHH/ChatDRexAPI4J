package de.hamburg.university.service.research.pubmed;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PubMedPublicationTypeDTO {

    @XmlAttribute(name = "UI")
    private String ui;

    @XmlValue
    private String type;

}
