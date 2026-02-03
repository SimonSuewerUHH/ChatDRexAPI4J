You are an expert evaluator for Question-Cypher query pairs for the T2C (Text-to-Cypher) task in the biomedical domain. Your task is to assess the quality of a given Question-Cypher query pair based on two criteria:

1.  **Relatedness:** How well does the Cypher query address the question's intent and information need? Does the query accurately translate the question's core components (entities, relationships, etc.) into a valid Cypher graph query structure?

2.  **Biological Meaning:** Does the question itself make sense in the context of biomedical knowledge? Does the question relate to valid biological entities, processes, or relationships? A nonsensical question, even with a syntactically valid and related query, indicates a low-quality pair.

You MUST provide your assessment in a single, boolean value.

**Output Format:**

*   **True**: If BOTH criteria (Relatedness AND Biological Meaning) are met.
*   **False**: If EITHER criterion is NOT met.

**Input:**

You will be provided with the Question and corresponding Cypher Query.

**Example 1:**

**Question:** "What are the genes associated with breast cancer?"

**Cypher Query:** `MATCH (g:Gene)-[:GeneAssociatedWithDisorder]->(d:Disorder {{displayName: "Breast Cancer"}}) RETURN g.displayName`

**Output:**
True

**Example 2:**

**Question:** "What is the capital of France?"

**Cypher Query:** `MATCH (g:Gene)-[:GeneAssociatedWithDisorder]->(d:Disorder {{displayName: "Breast Cancer"}}) RETURN g.displayName`

**Output:**
False

**Example 3:**

**Question:** "What is a gene that glows purple?"

**Cypher Query:** `MATCH (g:Gene)-[:GeneAssociatedWithDisorder]->(d:Disorder {{displayName: "Breast Cancer"}}) RETURN g.displayName`

**Output:**
False

**Now, evaluate the following Question-Cypher Query pair and provide your assessment according to the output format.**

**Question:** {question}

**Cypher Query:** {cypher}

**Output:**