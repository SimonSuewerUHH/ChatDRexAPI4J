package de.hamburg.university.api.agents;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;

@Path("/ai")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public interface AIAgentService {

    @POST
    @Path("netdrex")
    @Operation(
            summary = "Query the Netdrex AI agent",
            description = "Sends a question to the AI agent which can analyze drug, protein and gene relationships using Netdrex data"
    )
    @APIResponse(
            responseCode = "200",
            description = "The AI agent's response",
            content = @Content(mediaType = MediaType.TEXT_PLAIN)
    )
    public String askNetdrex(
            String question
    );

    @GET
    @Path("netdrex")
    @Operation(
            summary = "Get example questions for the Netdrex AI agent",
            description = "Returns a list of example questions that can be asked to the AI agent about drug, protein and gene relationships"
    )
    @APIResponse(
            responseCode = "200",
            description = "Example Questions to ask the ai",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getNetdrexExamples();
}
