package de.hamburg.university.api.agents;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

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
    public String ask(
            String question
    );
}
