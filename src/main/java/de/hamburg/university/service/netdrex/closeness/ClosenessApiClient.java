package de.hamburg.university.service.netdrex.closeness;


import de.hamburg.university.service.netdrex.NetdrexJobApi;
import de.hamburg.university.service.netdrex.trustrank.TrustRankSeedPayloadDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankStatusResponseDTO;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;

@Path("/closeness")
@RegisterRestClient(configKey = "netdrex-client")
public interface ClosenessApiClient extends NetdrexJobApi<ClosenessSeedPayloadDTO, ClosenessStatusResponseDTO> {

    @POST
    @Path("submit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> submit(ClosenessSeedPayloadDTO payload);

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<ClosenessStatusResponseDTO> status(@QueryParam("uid") String uid);
}
