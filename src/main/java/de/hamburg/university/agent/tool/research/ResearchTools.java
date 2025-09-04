package de.hamburg.university.agent.tool.research;

import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.service.research.semanticscholar.SemanticScholarServiceImpl;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.MemoryId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static io.quarkus.arc.ComponentsProvider.LOG;

@ApplicationScoped
public class ResearchTools {

    @Inject
    SemanticScholarServiceImpl semanticScholarService;

    @Inject
    ChatWebsocketSender chatWebsocketSender;

    @Tool("Useful for searching academic papers and articles on Semantic Scholar. "
            + "Provide a concise summary of the most relevant papers including title, authors, year, abstract, URL, and DOI. "
            + "If no results are found, respond with 'No results found.'")
    public String querySemanticScholar(String query, @MemoryId String sessionId) {
        chatWebsocketSender.sendToolStartResponse(query, sessionId);
        try {
            var response = semanticScholarService.executeQuery(query);
            if (response != null && response.getData() != null) {
                StringBuilder sb = new StringBuilder();
                for (var paper : response.getData()) {
                    sb.append("Title: ").append(paper.getTitle()).append("\n");
                    sb.append("Authors: ");
                    if (paper.getAuthors() != null) {
                        List<String> authorNames = new ArrayList<>();
                        for (var author : paper.getAuthors()) {
                            authorNames.add(author.getName());
                        }
                        sb.append(String.join(", ", authorNames));
                    }
                    sb.append("\n");
                    sb.append("Year: ").append(paper.getYear()).append("\n");
                    sb.append("Abstract: ").append(paper.getAbstractText()).append("\n");
                    sb.append("URL: ").append(paper.getUrl()).append("\n");
                  //  sb.append("DOI: ").append(paper.getExternalIds().getDoi()).append("\n");
                    sb.append("-----\n");
                }
                return sb.toString();
            } else {
                return "No results found.";
            }
        } catch (Exception e) {
            LOG.error("Error querying Semantic Scholar", e);
            return "Error querying Semantic Scholar: " + e.getMessage();
        }
    }

}
