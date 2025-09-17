# chatdrex

ChatDRex is a Quarkus-based backend platform that provides AI-powered workflows for drug repurposing, network-based analysis, and knowledge graph queries.
This project uses Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```
Or Run if you have Docker installed:
```shell script
docker compose up
```

## Configuration (application.properties)

All main configuration is done in src/main/resources/application.properties.

### LLM Configuration
 Please change the following properties to match your LLM provider and API key.
```
quarkus.langchain4j.openai.base-url=https://chat.cosy.bio/api

quarkus.langchain4j.openai.chat-model.model-name=gpt-oss:latest
quarkus.langchain4j.openai.api-key=<KEY>
```

### Guardrails (Prompt Injection & Grounding)

```
chatdrex.guardtrails.prompt-injection.score.threshold=0.7
chatdrex.guardtrails.prompt-injection.agent.enabled=true
chatdrex.guardtrails.prompt-injection.enabled=true

chatdrex.guardtrails.grounding.score.threshold=0.7
chatdrex.guardtrails.grounding.agent.enabled=true
```


##  API Documentation

ChatDRex provides two main interfaces for exploring the API:

### 1. **Swagger UI**
- Available at: [http://localhost:8080/q/swagger-ui](http://localhost:8080/q/swagger-ui)
- Explore and test all REST endpoints directly in the browser.

### 2. **Model Context Protocol (MCP)**
- MCP endpoints provide structured API access for **AI agents**.
- Endpoint base path:
  ```
  http://localhost:8080/mcp/
  ```
- Example:
    - `/mcp/tools` → list available AI tools (Cypher, DIAMOnD, DIGEST, TrustRank)
    - `/mcp/chat` → AI-driven chat interface

---

## 🧩 Features

- **Knowledge Graph Queries** (Neo4j, schema-constrained Cypher)
- **Disease Module Detection** (DIAMOnD algorithm)
- **Functional Enrichment** (DIGEST)
- **Trust-Based Ranking** (TrustRank for drugs & proteins)
- **LLM Guardrails** (prompt-injection detection, grounding validation)
- **Auth Integration** (Keycloak for secure endpoints)
- **API-first** design with Swagger + MCP

---

## 🛠️ Tech Stack

- [Quarkus](https://quarkus.io/) – Supersonic Subatomic Java
- LangChain4j – LLM integration and guardrails
