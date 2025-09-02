package de.hamburg.university.service.research.pubmed;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PubMedJournalDTO {

    @XmlElement(name = "Title")
    private String title;

    @XmlElement(name = "ISSN")
    private String issn;

    @XmlElement(name = "JournalIssue")
    private PubMedJournalIssueDTO journalIssue;
}
