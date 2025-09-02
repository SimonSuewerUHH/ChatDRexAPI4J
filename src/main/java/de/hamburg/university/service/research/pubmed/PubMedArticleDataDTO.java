package de.hamburg.university.service.research.pubmed;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PubMedArticleDataDTO {

    @XmlElement(name = "Journal")
    private PubMedJournalDTO journal;

    @XmlElement(name = "ArticleTitle")
    private String articleTitle;

    @XmlElement(name = "Abstract")
    private PubMedArticleAbstractDTO abstractText;


    @XmlElement(name = "ELocationID")
    private List<PubMedELocationIDDTO> locationIDs;

    @XmlElement(name = "AuthorList")
    private PubMedAuthorListDTO authorList;

    @XmlElement(name = "PublicationTypeList")
    private List<PubMedPublicationTypeDTO> publicationTypeList;

}
