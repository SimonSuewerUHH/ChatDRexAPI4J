package de.hamburg.university.service.research.pubmed;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@XmlRootElement(name = "PubmedArticleSet")
@XmlAccessorType(XmlAccessType.FIELD)
public class PubMedArticleSetDTO {

    @XmlElement(name = "PubmedArticle")
    private List<PubMedArticleDTO> pubmedArticles;
}
