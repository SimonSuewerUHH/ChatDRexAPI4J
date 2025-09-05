package de.hamburg.university.api.tools.netdrex;

import de.hamburg.university.service.netdrex.diamond.DiamondResultsDTO;
import de.hamburg.university.service.netdrex.diamond.SeedPayloadDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankResultDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankSeedPayloadDTO;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;


@Path("/netdrex")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "netdrex Wrapper", description = "Wrapper endpoints for netdrex tools.")
public interface NetdrexWrapperService {

    @POST
    @Path("/diamond/run")
    @Operation(
            summary = "Run Diamond tool",
            description = "Submits seeds to Diamond and returns the final Diamond results."
    )
    @APIResponse(
            responseCode = "200",
            description = "Diamond results.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = DiamondResultsDTO.class),
                    examples = @ExampleObject(name = "request", value = """
                            { "seeds": ["TP53","EGFR","BRCA1"] }
                            """)
            )
    )
    @APIResponse(responseCode = "400", description = "Invalid payload")
    @APIResponse(responseCode = "500", description = "Server error")
    Uni<DiamondResultsDTO> runDiamond(@Valid SeedPayloadDTO payload);

    @POST
    @Path("/trustrank/run")
    @Operation(
            summary = "Run TrustRank tool",
            description = "Submits seeds to TrustRank and returns the final TrustRank results."
    )
    @APIResponse(
            responseCode = "200",
            description = "TrustRank results.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = TrustRankResultDTO.class),
                    examples = @ExampleObject(name = "request", value = """
                            { "seedProteins": ["uniprot.Q9UBT6","uniprot.P04637"], "alpha": 0.85 }
                            """)
            )
    )
    @APIResponse(responseCode = "400", description = "Invalid payload")
    @APIResponse(responseCode = "500", description = "Server error")
    Uni<TrustRankResultDTO> runTrustRank(@Valid TrustRankSeedPayloadDTO payload);
}