package de.hamburg.university.api.agents;

import de.hamburg.university.agent.bot.NetdrexBot;
import de.hamburg.university.agent.bot.ResearchBot;
import de.hamburg.university.agent.bot.kg.NetdrexKGGraph;
import de.hamburg.university.agent.tool.netdrex.kg.NetdrexKGTool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AiAgentServiceImpl implements AIAgentService {
    @Inject
    NetdrexBot netdrexBot;

    @Inject
    ResearchBot researchBot;

    @Inject
    NetdrexKGTool netdrexKGTool;

    @Override
    public String askNetdrex(String question) {
        String id = UUID.randomUUID().toString();
        return netdrexBot.answer(id, question, "");
    }

    @Override
    public List<String> getNetdrexExamples() {
        return List.of(
                "Get info for these IDs: uniprot.P04637, uniprot.Q9UBT6, drugbank.DB00001, entrez.7157",
                "From UniProt entry uniprot.P04637, list the gene names and the primary gene symbol.",
                "Return the protein names for uniprot.P31749 and uniprot.Q9Y6K9.",
                "Given IDs drugbank.DB00316 and drugbank.DB01050, return display names and any linked proteins.",
                "Given entrez.7157 and entrez.1956, return their UniProt accessions.",
                "Map these Entrez IDs to UniProt accessions: 23616, 9912, 55114, 30011, 23109, 7827, 4868, 6654, 25, 3055",
                "Only return the list of Entrez IDs from the previous mapping as a plain JSON list.",
                "Only return the list of UniProt accessions as a plain JSON list.",
                "Run TrustRank with seeds (UniProt): uniprot.P04637, uniprot.P31749, uniprot.Q9Y243. Return top 15 candidate drugs with scores.",
                "Run TrustRank for seeds uniprot.Q9UBT6 and uniprot.P38398. Return seed genes, seed proteins, and top 10 drugs (names resolved).",
                "TrustRank on seeds uniprot.P01308, uniprot.P01375; provide a short rationale per top-5 drug.",
                "Using DIAMOND, expand seeds TP53, BRCA1 for 25 closest proteins and include edges.",
                "DIAMOND on seeds uniprot.P04637, uniprot.P38398; return a “drugstone-like” network JSON (nodes/edges).",
                "Take Entrez IDs (23616, 9912, 55114). Map to UniProt, run DIAMOND for 20 proteins, then run TrustRank; summarize top 5 drugs.",
                "Given uniprot.P04637 and uniprot.P31749, fetch protein info, expand with DIAMOND, translate drugbank IDs to names, and produce a Markdown summary.",
                "Build a minimal protein–drug network for type-2 diabetes (seeds: uniprot.P01308, uniprot.P01375).",
                "Try resolving info for malformed IDs: uniprotP04637, drugbank-DB00001, entrez7157. Return which ones failed and why.",
                "Call getInfo with mixed valid/invalid IDs and return per-ID status."
        );
    }

    @Override
    public String askResearch(String question) {
        String id = UUID.randomUUID().toString();
        return researchBot.answer(id, question, "");
    }

    @Override
    public NetdrexKGGraph splitKGQuestions(String question) {
        return netdrexKGTool.decomposeToNodes(question);
    }

    @Override
    public String generateCypher(String question) {
        return netdrexKGTool.generateCypher(question);

    }

    @Override
    public String answerKG(String question) {
        return netdrexKGTool.answer(question);
    }

    @Override
    public List<String> getResearchExamples() {
        return List.of(
                "Recent advances (2022–2025) in federated learning for biomedicine focusing on privacy and utility trade-offs",
                "Key graph neural network methods for blood–brain barrier permeability prediction",
                "Best datasets and benchmarks for Type 2 diabetes risk prediction from EHR/omics",
                "Contrastive learning for multimodal patient digital twins: state of the art",
                "Bias detection & mitigation techniques in clinical ML models (survey + practice)"
        );
    }
}
