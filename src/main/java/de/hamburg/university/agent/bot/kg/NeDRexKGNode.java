package de.hamburg.university.agent.bot.kg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.langchain4j.model.output.structured.Description;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NeDRexKGNode {

    @Description("One of: disorder, drug, gene, genomic_variant, go, pathway, phenotype, protein, side_effect, signature, tissue")
    private String nodeType;

    @Description("Concrete biomedical value or term, e.g. 'breast cancer', 'TP53', 'insulin receptor', 'PI3K-Akt signaling pathway'")
    private String nodeValue;

    @Description("Short context (<= 12 words) clarifying the node's role, e.g. 'approved treatments', 'interacts with insulin receptor'")
    private String subQuestion;

    @Description("Whether this node needs to be filtered for relevance in the context of the user's question")
    private Boolean needsFilter;
}