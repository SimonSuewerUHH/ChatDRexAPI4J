package de.hamburg.university.service.netdrex.trustrank;


import de.hamburg.university.service.netdrex.NetdrexJobApi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;

@Path("/trustrank")
@RegisterRestClient(configKey = "netdrex-client")
public interface TrustRankApiClient extends NetdrexJobApi<TrustRankSeedPayloadDTO, TrustRankStatusResponseDTO> {

    @POST
    @Path("submit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> submit(TrustRankSeedPayloadDTO payload);

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<TrustRankStatusResponseDTO> status(@QueryParam("uid") String uid);
}
