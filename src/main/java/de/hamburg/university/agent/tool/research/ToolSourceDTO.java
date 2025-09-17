package de.hamburg.university.agent.tool.research;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hamburg.university.service.research.semanticscholar.SemanticScholarPaperDTO;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApplicationScoped
@NoArgsConstructor
public class ToolSourceDTO {
    private String paperId;
    private String doi;
    private String title;
    private Integer publishedYear;
    private String authors;
    private String journal;
    @JsonProperty("abstract")
    private String abstractText;

    public ToolSourceDTO(SemanticScholarPaperDTO paper) {
        this.paperId = paper.getPaperId();
        this.doi = paper.getExternalIds() != null ? paper.getExternalIds().getDoi() : null;
        this.title = paper.getTitle();
        this.publishedYear = paper.getYear();
        if (paper.getAuthors() != null) {
            StringBuilder authorNames = new StringBuilder();
            for (var author : paper.getAuthors()) {
                if (authorNames.length() > 0) {
                    authorNames.append(", ");
                }
                authorNames.append(author.getName());
            }
            this.authors = authorNames.toString();
        } else {
            this.authors = "";
        }
        this.journal = paper.getVenue();
        this.abstractText = paper.getAbstractText();
    }
}
