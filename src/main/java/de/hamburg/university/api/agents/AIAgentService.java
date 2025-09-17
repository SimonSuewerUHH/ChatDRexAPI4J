package de.hamburg.university.api.agents;

import de.hamburg.university.agent.bot.kg.NeDRexKGGraph;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;

@Path("/ai")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public interface AIAgentService {

    @POST
    @Path("netdex")
    @Operation(
            summary = "Query the NeDRex AI agent",
            description = "Sends a question to the AI agent which can analyze drug, protein and gene relationships using NeDRex data"
    )
    @APIResponse(
            responseCode = "200",
            description = "The AI agent's response",
            content = @Content(mediaType = MediaType.TEXT_PLAIN)
    )
    public String askNeDRex(
            String question
    );

    @GET
    @Path("nedrex")
    @Operation(
            summary = "Get example questions for the NeDRex AI agent",
            description = "Returns a list of example questions that can be asked to the AI agent about drug, protein and gene relationships"
    )
    @APIResponse(
            responseCode = "200",
            description = "Example Questions to ask the ai",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getNeDRexExamples();


    @POST
    @Path("research")
    @Operation(
            summary = "Query the Research AI agent",
            description = "Sends a question to the AI agent which can analyze drug, protein and gene relationships using NeDRex data"
    )
    @APIResponse(
            responseCode = "200",
            description = "The AI agent's response",
            content = @Content(mediaType = MediaType.TEXT_PLAIN)
    )
    public String askResearch(
            String question
    );

    @POST
    @Path("kg/split")
    @Operation(
            summary = "Query the Research AI agent",
            description = "Sends a question to the AI agent which can analyze drug, protein and gene relationships using NeDRex data"
    )
    @RequestBody(
            required = true,
            description = "Biomedical research question to decompose",
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN,
                    examples = {
                            @ExampleObject(
                                    name = "Drugs for breast cancer",
                                    value = "What drugs are approved for treating breast cancer?"
                            ),
                            @ExampleObject(
                                    name = "Genes with insulin receptors",
                                    value = "Which genes interact with insulin receptors in diabetes?"
                            ),
                            @ExampleObject(
                                    name = "Pathways in Parkinson's",
                                    value = "Show pathways involved in Parkinson's disease phenotypes in brain tissue"
                            ),
                            @ExampleObject(
                                    name = "Side effects of metformin",
                                    value = "What are the side effects of metformin in type 2 diabetes?"
                            ),
                            @ExampleObject(
                                    name = "Proteins linked to Alzheimer's",
                                    value = "Which proteins are linked to Alzheimer's disease progression?"
                            )
                    }
            )
    )
    @Produces(MediaType.APPLICATION_JSON)
    public NeDRexKGGraph splitKGQuestions(
            String question
    );

    @POST
    @Path("kg/cypher")
    @Operation(
            summary = "Query the Research AI agent",
            description = "Sends a question to the AI agent which can analyze drug, protein and gene relationships using NeDRex data"
    )
    @RequestBody(
            required = true,
            description = "Biomedical research question to decompose",
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN,
                    examples = {
                            @ExampleObject(
                                    name = "Drugs for breast cancer",
                                    value = "What drugs are approved for treating breast cancer?"
                            ),
                            @ExampleObject(
                                    name = "Genes with insulin receptors",
                                    value = "Which genes interact with insulin receptors in diabetes?"
                            ),
                            @ExampleObject(
                                    name = "Pathways in Parkinson's",
                                    value = "Show pathways involved in Parkinson's disease phenotypes in brain tissue"
                            ),
                            @ExampleObject(
                                    name = "Side effects of metformin",
                                    value = "What are the side effects of metformin in type 2 diabetes?"
                            ),
                            @ExampleObject(
                                    name = "Proteins linked to Alzheimer's",
                                    value = "Which proteins are linked to Alzheimer's disease progression?"
                            )
                    }
            )
    )
    public String generateCypher(
            String question
    );

    @POST
    @Path("kg/answer")
    @Operation(
            summary = "Query the Research AI agent",
            description = "Sends a question to the AI agent which can analyze drug, protein and gene relationships using NeDRex data"
    )
    @RequestBody(
            required = true,
            description = "Biomedical research question to decompose",
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN,
                    examples = {
                            @ExampleObject(
                                    name = "Drugs for breast cancer",
                                    value = "What drugs are approved for treating breast cancer?"
                            ),
                            @ExampleObject(
                                    name = "Genes with insulin receptors",
                                    value = "Which genes interact with insulin receptors in diabetes?"
                            ),
                            @ExampleObject(
                                    name = "Pathways in Parkinson's",
                                    value = "Show pathways involved in Parkinson's disease phenotypes in brain tissue"
                            ),
                            @ExampleObject(
                                    name = "Side effects of metformin",
                                    value = "What are the side effects of metformin in type 2 diabetes?"
                            ),
                            @ExampleObject(
                                    name = "Proteins linked to Alzheimer's",
                                    value = "Which proteins are linked to Alzheimer's disease progression?"
                            )
                    }
            )
    )
    public String answerKG(
            String question
    );

    @GET
    @Path("research")
    @Operation(
            summary = "Get example questions for the Research AI agent",
            description = "Returns a list of example questions that can be asked to the AI agent about drug, protein and gene relationships"
    )
    @APIResponse(
            responseCode = "200",
            description = "Example Questions to ask the ai",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getResearchExamples();
}
