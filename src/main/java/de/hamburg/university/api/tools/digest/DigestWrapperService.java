package de.hamburg.university.api.tools.digest;

import de.hamburg.university.service.digest.DigestToolResultDTO;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/digest")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "DIGEST Wrapper", description = "Thin REST wrapper around DIGEST set/subnetwork submissions.")
public interface DigestWrapperService {

    @POST
    @Path("/set")
    @Operation(
            summary = "Submit a gene set to DIGEST",
            description = "Accepts a list of target identifiers and submits them as a **set** to the DIGEST backend. Returns the formatted result as a string."
    )
    @APIResponse(
            responseCode = "200",
            description = "Submission accepted; formatted result returned.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.STRING),
                    examples = @ExampleObject(name = "result", value = "\"The provided gene set has annotations in 3 databases...\"")
            )
    )
    @APIResponse(responseCode = "400", description = "Invalid request payload.")
    @APIResponse(responseCode = "500", description = "Server error while processing request.")
    Uni<DigestToolResultDTO> submitSet(DigestTargetsRequestDTO body);

    @POST
    @Path("/subnetwork")
    @Operation(
            summary = "Submit a subnetwork to DIGEST",
            description = "Accepts a list of target identifiers and submits them as a **subnetwork** to the DIGEST backend. Returns the formatted result as a string."
    )
    @APIResponse(
            responseCode = "200",
            description = "Submission accepted; formatted result returned.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.STRING),
                    examples = @ExampleObject(name = "result", value = "\"The provided gene set has annotations in 2 databases...\"")
            )
    )
    @APIResponse(responseCode = "400", description = "Invalid request payload.")
    @APIResponse(responseCode = "500", description = "Server error while processing request.")
    Uni<DigestToolResultDTO> submitSubnetwork(DigestTargetsRequestDTO body);
}
