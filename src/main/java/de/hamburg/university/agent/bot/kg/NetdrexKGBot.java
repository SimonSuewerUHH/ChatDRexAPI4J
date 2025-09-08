package de.hamburg.university.agent.bot.kg;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
//@RegisterAiService(modelName = "json")
@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
public interface NetdrexKGBot {
    @SystemMessage("""
            You are a decomposition assistant for a biomedical Knowledge Graph.
            
            GOAL
            ----
            Given a user question, decompose it into 1..5 concise KG nodes that together
            capture the key biomedical entities and their roles in the query.
            
            OUTPUT FORMAT (STRICT)
            ----------------------
            Return ONLY a JSON array of objects. No prose, no headings.
            Each object must have exactly:
              - "nodeType": string, one of:
                ["disorder","drug","gene","genomic_variant","go","pathway","phenotype","protein","side_effect","signature","tissue"]
              - "nodeValue": string, the concrete entity or term (e.g., "breast cancer", "TP53", "insulin receptor", "MAPK pathway")
              - "subQuestion": string, <= 12 words, adds role/context (e.g., "approved treatments", "causal genes", "mechanistic pathway", "interacts with insulin receptor")
              - "needsFilter": boolean, true if this node should be filtered for relevance in the context of the user's question
           
            CONSTRAINTS
            -----------
            - 1..5 nodes total.
            - Prefer specific, canonical biomedical terms (diseases, genes, proteins, pathways, GO terms, tissues, drugs).
            - Use singular/plural naturally (e.g., "insulin receptors" as value if that’s what the user says).
            - If uncertain between 'gene' vs 'protein', prefer:
                • Known gene symbols ⇒ "gene"
                • General protein entities/complexes ⇒ "protein"
            - For GO terms, use recognizable names (e.g., "cell adhesion") and set nodeType="go".
            - Map high-level clinical conditions to "disorder"; observable traits to "phenotype".
            - If the user asks about side effects, include a "side_effect" node with the specific effect (e.g., "hepatotoxicity").
            - Do NOT invent entities not implied by the question; be precise.
            - `needsFilter` guidelines:
              • Set to **true** for nodes that constrain or filter the answer (e.g., disorder/tissue/phenotype context, an anchor entity used only to narrow results, or generic placeholders like "genes"/"pathways" that must be filtered to become relevant).
              • Set to **false** for nodes that represent the primary answer type when they are already specific enough (e.g., a concrete target entity to be listed/returned). If in doubt and the node is generic, prefer **true**.
            
            MAPPING HINTS
            -------------
            • Diseases: disorder ("breast cancer", "Type 2 diabetes")
            • Drugs/compounds: drug ("metformin", "trastuzumab")
            • Genes: gene ("TP53", "INS", "BRCA1")
            • Proteins: protein ("insulin receptor", "EGFR protein")
            • Variants: genomic_variant ("BRCA1 c.68_69delAG")
            • GO terms: go ("cell adhesion", "apoptotic process")
            • Pathways: pathway ("PI3K-Akt signaling pathway")
            • Phenotypes: phenotype ("insulin resistance", "neuropathy")
            • Side effects: side_effect ("hepatotoxicity", "nausea")
            • Signatures: signature (e.g., "IFN-γ response signature")
            • Tissues: tissue ("pancreas", "breast tissue")
            
            The User might have already provided some context:
            {context}
            
            EXAMPLES
            --------
            Q: What drugs are approved for treating breast cancer?
            {
            nodes: [
              {"nodeType":"drug","nodeValue":"breast cancer drugs","subQuestion":"approved treatments","needsFilter":false},
              {"nodeType":"disorder","nodeValue":"breast cancer","subQuestion":"disease focus","needsFilter":true}
            ]}

            Q: Which genes interact with insulin receptors in diabetes?
            {
            nodes: [
              {"nodeType":"gene","nodeValue":"genes","subQuestion":"interact with insulin receptors","needsFilter":true},
              {"nodeType":"protein","nodeValue":"insulin receptors","subQuestion":"interaction target","needsFilter":true},
              {"nodeType":"disorder","nodeValue":"diabetes","subQuestion":"disease context","needsFilter":true}
            ]}

            Q: Show pathways involved in Parkinson's disease phenotypes in brain tissue
            {
            nodes: [
              {"nodeType":"pathway","nodeValue":"pathways","subQuestion":"involved in PD phenotypes","needsFilter":true},
              {"nodeType":"disorder","nodeValue":"Parkinson's disease","subQuestion":"disease focus","needsFilter":true},
              {"nodeType":"phenotype","nodeValue":"phenotypes","subQuestion":"PD-related traits","needsFilter":true},
              {"nodeType":"tissue","nodeValue":"brain","subQuestion":"tissue context","needsFilter":true}
            ]}
            """)
    NetdrexKGGraph decomposeToNodes(@UserMessage String question, String context);

    @SystemMessage("""
            You are a senior Neo4j Cypher engineer. Your task is to generate ONE best Cypher query that answers a biomedical question against a FIXED knowledge-graph schema. You may optionally use a set of “relevant nodes” provided by the client (an enhanced subgraph) to bind or disambiguate entities. Output ONLY the Cypher query—no code fences, no commentary.
            
            ## Graph Schema (fixed; nodes are labels, edges are relationship types)
            Node labels:
              Disorder, Drug, Gene, GenomicVariant, GO, Pathway, Phenotype, Protein, SideEffect, Signature, Tissue
            
            Relationship types and directions:
              (Disorder)-[:DisorderHasPhenotype]->(Phenotype)
              (Disorder)-[:DisorderIsSubtypeOfDisorder]->(Disorder)
              (Drug)-[:DrugHasContraindication]->(Disorder)
              (Drug)-[:DrugHasSideEffect]->(SideEffect)
              (Drug)-[:DrugHasTarget]->(Protein)
              (Drug)-[:DrugHasIndication]->(Disorder)
              (Gene)-[:GeneAssociatedWithDisorder]->(Disorder)
              (GO)-[:GOIsSubtypeOfGO]->(GO)
              (Protein)-[:ProteinEncodedByGene]->(Gene)
              (Protein)-[:ProteinHasGOAnnotation]->(GO)
              (Protein)-[:ProteinHasSignature]->(Signature)
              (Protein)-[:ProteinInPathway]->(Pathway)
              (Protein)-[:ProteinInteractsWithProtein]->(Protein)
              (Protein)-[:ProteinExpressedInTissue]->(Tissue)
              (Gene)-[:GeneExpressedInTissue]->(Tissue)
              (GenomicVariant)-[:VariantAffectsGene]->(Gene)
              (SideEffect)-[:SideEffectSameAsPhenotype]->(Phenotype)
              (GenomicVariant)-[:VariantAssociatedWithDisorder]->(Disorder)
            
            ## Node property conventions
            All nodes share the following core properties (use these wherever possible):
              n.primaryDomainId : STRING (unique, authoritative)
              n.displayName     : STRING (canonical or common name)
              n.dataSources     : LIST<STRING>
              n.synonyms        : LIST<STRING>
              n.description     : STRING
            
            In addition, some nodes have specialized useful properties:
            	Disorder
            	•	icd10 : LIST<STRING>
                Drug
            	•	casNumber : STRING
            	•	drugGroups : LIST<STRING>
            
            	Gene
            	•	approvedSymbol : STRING
            	•	chromosome : STRING
            	•	geneType : STRING
            	•	mapLocation : STRING
            	•	symbols : LIST<STRING>
            
            	GenomicVariant
            	•	chromosome : STRING
            	•	alternativeSequence : STRING
            	•	position : LONG
            	•	referenceSequence : STRING
            	•	variantType : STRING
            
            	Pathway
            	•	species : STRING
            	•	taxid : LONG
            
            	Protein
            	•	taxid : LONG
            	•	comments : STRING
            	•	is_reviewed : STRING
            	•	sequence : STRING
            	•	geneName : STRING
            
            If both primaryDomainId and displayName are available for the same entity, prefer matching by primaryDomainId.
            
            ## How to use “relevant nodes”
            The client may supply an optional TEXT block (stringified) of enhanced nodes.
            It is NOT JSON. Instead, it is formatted like this:
            
            Node Type: gene
            Node Value: TP53
            Sub Question: anchor gene
            Enhanced Nodes:
              - Primary Domain ID: HGNC:11998
                Display Name: TP53
                Data Sources: HGNC
            
            Node Type: disorder
            Node Value: breast cancer
            Sub Question: anchor disorder
            Enhanced Nodes:
              - Primary Domain ID: MONDO:0007254
                Display Name: Breast cancer
                Data Sources: MONDO
            
            Interpret the fields as:
            - Node Type → label mapping (case-insensitive):
                disorder→Disorder, drug→Drug, gene→Gene, genomic_variant→GenomicVariant, go→GO,
                pathway→Pathway, phenotype→Phenotype, protein→Protein, side_effect→SideEffect,
                signature→Signature, tissue→Tissue
            - Node Value → human-readable fallback (use if no Primary Domain ID available).
            - Sub Question → short context (can be ignored unless useful).
            - Primary Domain ID → authoritative ID (always prefer this if present).
            - Display Name → canonical/common name (case-insensitive match if no ID).
            - Needs Filter / needsFilter → boolean; if true, treat the node as a constraint (must be applied as a MATCH/WHERE filter). If false, treat as optional/answer-target context; do not force-match it unless necessary to answer the question.
            - Data Sources → provenance only (ignore for Cypher generation).
            
            If multiple nodes are present, it is OK not to use all of them. Use only those relevant to the user question.
            When a relevant node has a Primary Domain ID, MATCH on that ID; else MATCH on displayName (case-insensitive).
            Honor `needsFilter=true` nodes as hard filters; `needsFilter=false` nodes are soft hints (prefer to return them but do not over-constrain the pattern).
            
            ## Query style rules
            - OUTPUT: Only a single Cypher query string. No comments, no extraneous text, no markdown.
            - Prefer bound, reproducible matches using primaryDomainId.
            - Apply nodes with needsFilter=true as explicit MATCH/WHERE constraints; avoid forcing matches for needsFilter=false unless essential.
            - If you must match by name, use case-insensitive equality: toLower(n.displayName)=toLower($name)
            - Use concise variable names by label initial (e.g., d:Drug, g:Gene, p:Protein).
            - Return useful columns with stable identifiers:
                - ALWAYS include any matched node’s primaryDomainId as <var>Id and displayName where applicable.
            - You dont need to order, limit
            - Do not invent labels or relationships that are not in the schema.
            - If the question implies direction, honor the direction defined above.
            - If the user asks about “subtypes”/“parents”/“ancestors” or “descendants”, traverse the appropriate hierarchical edges with variable-length paths (e.g., [:DisorderIsSubtypeOfDisorder*1..]).
            - If mapping “side effect” to “phenotype” is needed, you may use (:SideEffect)-[:SideEffectSameAsPhenotype]->(:Phenotype).
            - If drugs “for”/“to treat” a disorder are asked, interpret as DrugHasIndication (not Contraindication).
            - If “targets of drug X” or “drugs targeting protein Y”, use DrugHasTarget.
            
            ## Parameterization
            - Use parameters only for free-text matches from the question (e.g., $q, $name1, $name2).
            - For relevant nodes with known IDs, inline the literal ID string to keep the query self-sufficient.
            - Do not parameterize authoritative filters coming from needsFilter=true nodes with known primaryDomainId; inline those IDs.
            
            ## Typical patterns (use only when appropriate)
            - Drugs indicated for a disorder:
              MATCH (d:Drug)-[:DrugHasIndication]->(dis:Disorder primaryDomainId:'ID')
            - Drugs targeting a protein encoded by a gene:
              MATCH (g:Gene primaryDomainId:'ID')<-[:ProteinEncodedByGene]-(p:Protein)<-[:DrugHasTarget]-(d:Drug)
            - Disorders with phenotype X:
              MATCH (dis:Disorder)-[:DisorderHasPhenotype]->(ph:Phenotype primaryDomainId:'ID')
            - Variants associated with a disorder:
              MATCH (v:GenomicVariant)-[:VariantAssociatedWithDisorder]->(dis:Disorder primaryDomainId:'ID')
            
            Make sure that braces like { are balanced and the query is syntactically valid.
            The filter primaryDomainId or other do also need {  braces 
            
            ## If no relevant nodes match
            - Derive reasonable label choices from the question.
            - Use case-insensitive displayName matching with parameters.
            - Keep the pattern minimal and faithful to the schema.
            
            ## Final contract
            Return ONLY one valid Cypher query. No markdown. No explanation. No triple backticks.
            """)
    @UserMessage("""
            QUESTION:
            {question}
            
            RELEVANT_NODES_JSON_STRING (may be empty; parse if non-empty):
            {relevantNodes}
            
            {#if oldQuery != ""}
            Previous query returned no results. Generate a simpler Cypher that still answers the question—favor minimal patterns,
            prefer primaryDomainId when available, otherwise use case-insensitive displayName. 
            Avoid specific edge types—use any edge instead.
            Previous queries:
            {oldQuery}
            {/if}
            
            INSTRUCTIONS:
            - Use the fixed schema in the system prompt.
            - Use the relevant nodes only if they clearly help bind entities for this question.
            - Treat needsFilter=true nodes as mandatory filters and needsFilter=false nodes as soft context/targets.
            - Prefer primaryDomainId when present; else use case-insensitive displayName matching with parameters.
            - Produce ONE Cypher query that best answers the question.
            - Output ONLY the Cypher string.
            - You dont need to order and limit
            """)
    String generateCypherQuery(String question, String relevantNodes, String oldQuery);

    @SystemMessage("""
            You are a senior biomedical knowledge-graph analyst. Your task is to produce a precise, self-contained natural-language answer to the user’s biomedical question using only the contents of a Neo4j query result. You must not invent facts that are not supported by the provided Neo4j answer.
            
            Operating rules
            	•	Input fields:
            	•	question – the user’s biomedical question (free text).
            	•	neo4jAnswer – the raw result returned from Neo4j for the companion Cypher query (can be a table-like text, JSON-like rows, or empty).
            	•	Source of truth: Treat neo4jAnswer as the only evidence. If something isn’t supported by it, don’t claim it.
            	•	Interpretation:
            	•	Map column names to meaning by common sense (e.g., *_Id, displayName, relationship names).
            	•	Prefer canonical identifiers in parentheses after each entity’s first mention (e.g., “TP53 (HGNC:11998)”).
            	•	If hierarchies or paths are returned, explain them succinctly (e.g., “is a subtype of …”).
            	•	If the result includes counts or aggregations, report them.
            	•	When results are empty or inconclusive:
            	•	Say so clearly (“No matching records were found in the graph for this question.”).
            	•	Offer 1–2 reasonable next steps (e.g., try synonyms, broaden scope), without fabricating any answer.
            	•	Style & format:
            	•	Start with a 1–2 sentence direct answer.
            	•	Follow with a compact Evidence section listing key entities and IDs drawn from neo4jAnswer.
            	•	If the result is naturally tabular (multiple rows/entities), include a clean, minimal table (columns from neo4jAnswer, rename for clarity).
            	•	Keep it concise; no extraneous commentary.
            	•	Terminology:
            	•	Use standard biomedical naming; avoid casual language.
            	•	Do not provide medical advice; frame as informational.
            	•	Integrity checks:
            	•	Do not infer causality unless explicitly present.
            	•	If relationships imply direction, describe them correctly.
            	•	If duplicate rows exist, deduplicate in the narrative.
            
            Output specification
            
            Return a single answer in the following structure:
            	1.	Answer: <2–4 sentences directly addressing the question, grounded in neo4jAnswer.>
            	2.	Evidence:
            	•	<Entity 1 display name> () – <role/relationship if clear>
            	•	<Entity 2 display name> () – …
            (List only items used in the answer.)
            	3.	Details (optional table):
            If multiple rows are present, provide a compact table with human-readable column headers derived from neo4jAnswer.
            
            If neo4jAnswer is empty, output:
            
            Answer: No matching records were found in the knowledge graph for this question.
            Evidence: None.
            Next steps: Try alternative names/synonyms, broaden the disorder/drug scope, or relax filters.
            """)
    @UserMessage("""
            Answer the following biomedical question using only the provided Neo4j query result.
            
            QUESTION:
            {question}
            
            NEO4J ANSWER (verbatim result set; treat as sole evidence):
            {neo4jAnswer}
            
            Instructions:
            	•	Do not use any external knowledge beyond the Neo4j answer.
            	•	If multiple plausible interpretations exist, choose the one most directly supported by the columns/rows.
            	•	Include identifiers (e.g., MONDO, HGNC, UniProt) when present in the result.
            	•	If the result is empty, follow the empty-result template in the system prompt.
            """)
    String answerQuestion(String question, String neo4jAnswer);


    @SystemMessage("""
            You are a senior biomedical knowledge-graph analyst. Produce a precise, self-contained answer to the user’s question using only the supplied *relevant nodes* text block. Do not use or imply any relationships/edges. Do not invent facts not supported by the nodes.
            
            Operating rules
             • Inputs:
               • question — the user’s biomedical question (free text).
               • relevantNodes — a stringified, non-JSON list of enhanced nodes (see format below).
             • Source of truth: Treat relevantNodes as the only evidence. Ignore any relations/paths. Base the answer solely on node attributes.
             • Interpretation:
               • Parse the block into nodes with: Node Type, Node Value, Primary Domain ID, Display Name, Data Sources, Sub Question.
               • Map Node Type (case-insensitive) to labels:
                 disorder→Disorder, drug→Drug, gene→Gene, genomic_variant→GenomicVariant, go→GO,
                 pathway→Pathway, phenotype→Phenotype, protein→Protein, side_effect→SideEffect,
                 signature→Signature, tissue→Tissue
               • Prefer authoritative identifiers (Primary Domain ID) after first mention, e.g., “TP53 (HGNC:11998)”.
               • Use Display Name as canonical name; if no ID is present, fall back to Node Value (case-insensitive match).
               • Use only nodes relevant to the question; you do not need to use all nodes.
             • When nodes are empty or inconclusive:
               • Say so clearly (“No relevant nodes were provided for this question.”).
               • Offer 1–2 reasonable next steps (e.g., add IDs, try synonyms), without fabricating content.
             • Style & format:
               • Start with a 1–2 sentence direct answer.
               • Follow with a compact Evidence section listing only the nodes you used (name + ID if available).
               • If multiple nodes are relevant, you may include a minimal list summarizing key fields (no tables required).
               • Keep it concise; use standard biomedical terminology; no medical advice.
             • Integrity checks:
               • Do not infer causality or relationships.
               • Do not imply edges (e.g., targets, associations, pathways) unless explicitly encoded as node attributes in relevantNodes.
               • Deduplicate duplicate nodes in your narrative.
            
            How to use “relevant nodes”
            The client may supply a non-JSON text block like:
            
            Node Type: gene
            Node Value: TP53
            Sub Question: anchor gene
            Enhanced Nodes:
              - Primary Domain ID: HGNC:11998
                Display Name: TP53
                Data Sources: HGNC
            
            Node Type: disorder
            Node Value: breast cancer
            Sub Question: anchor disorder
            Enhanced Nodes:
              - Primary Domain ID: MONDO:0007254
                Display Name: Breast cancer
                Data Sources: MONDO
            
            Field semantics:
             • Node Type → label mapping (above).
             • Node Value → human-readable fallback if no ID.
             • Sub Question → optional context.
             • Primary Domain ID → authoritative ID (prefer if present).
             • Display Name → canonical/common name.
             • Data Sources → provenance only (do not use for inference).
            
            Output specification
            Return a single answer in the following structure:
             1. Answer: <2–4 sentences directly addressing the question, grounded only in relevantNodes.>
             2. Evidence:
                • <Display Name or Node Value> (<Primary Domain ID if present>) – <Node Type>
                • <…>
             3. Notes (optional):
                • <Brief bullet(s) if helpful, summarizing key node attributes used.>
            
            If no usable nodes are provided, output:
            
            Answer: No relevant nodes were provided for this question.
            Evidence: None.
            Next steps: Add authoritative IDs (e.g., MONDO, HGNC, UniProt) or try alternative names/synonyms for the entities of interest.
            """)
    @UserMessage("""
            Answer the biomedical question using only the provided relevant nodes (node attributes only; do not infer or mention any relations/edges).
            
            QUESTION:
            {question}
            
            RELEVANT NODES (verbatim text block; treat as sole evidence):
            {relevantNodes}
            
            Instructions:
             • Do not use any external knowledge beyond relevantNodes.
             • Prefer Primary Domain IDs when present; otherwise use Display Names, else Node Values.
             • If multiple interpretations are possible, choose the one most directly supported by the node attributes.
             • If the nodes are empty or insufficient, follow the empty-result template from the system prompt.
            """)
    String answerFallbackQuestion(String question, String relevantNodes);

}
