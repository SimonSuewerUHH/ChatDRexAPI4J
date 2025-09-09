package de.hamburg.university.api.tools.netdrex;

import de.hamburg.university.service.netdrex.NetdrexAPIInfoDTO;
import de.hamburg.university.service.netdrex.diamond.DiamondResultsDTO;
import de.hamburg.university.service.netdrex.diamond.SeedPayloadDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankResultDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankSeedPayloadDTO;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;


@Path("/netdrex")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "netdrex Wrapper", description = "Wrapper endpoints for netdrex tools.")
public interface NetdrexWrapperService {

    @GET
    @Path("info")
    @APIResponse(responseCode = "500", description = "Server error")
    NetdrexAPIInfoDTO query(@QueryParam("q") String query);


    @POST
    @Path("/diamond/run")
    @Operation(
            summary = "Run Diamond tool",
            description = "Submits seeds to Diamond and returns the final Diamond results."
    )
    @APIResponse(responseCode = "400", description = "Invalid payload")
    @APIResponse(responseCode = "500", description = "Server error")
    Uni<DiamondResultsDTO> runDiamond(
            @RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SeedPayloadDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "default",
                                            value = "{\n" +
                                                    "  \"seeds\": [\"Q9Y5Y6\", \"Q9Y2D0\", \"Q96MZ4\"],\n" +
                                                    "  \"n\": 50,\n" +
                                                    "  \"alpha\": 1,\n" +
                                                    "  \"network\": \"DEFAULT\",\n" +
                                                    "  \"edges\": \"all\"\n" +
                                                    "}"
                                    )
                            }
                    )
            )
            @Valid SeedPayloadDTO payload
    );

    @POST
    @Path("/trustrank/run")
    @Operation(
            summary = "Run TrustRank tool",
            description = "Submits seeds to TrustRank and returns the final TrustRank results."
    )
    @APIResponse(responseCode = "400", description = "Invalid payload")
    @APIResponse(responseCode = "500", description = "Server error")
    Uni<TrustRankResultDTO> runTrustRank(
            @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TrustRankSeedPayloadDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "default",
                                            value = "{\n" +
                                                    "  \"seeds\": [\n" +
                                                    "    \"Q9Y5Y6\", \"Q9Y2D0\", \"Q96MZ4\", \"Q96T92\", \"Q9Y4K3\",\n" +
                                                    "    \"Q15375\", \"P35268\", \"P35555\", \"P00441\", \"P22301\"\n" +
                                                    "  ],\n" +
                                                    "  \"damping_factor\": 0.85,\n" +
                                                    "  \"only_direct_drugs\": false,\n" +
                                                    "  \"only_approved_drugs\": false,\n" +
                                                    "  \"N\": 10\n" +
                                                    "}"
                                    )
                            }
                    )
            )
            @Valid TrustRankSeedPayloadDTO payload
    );
}