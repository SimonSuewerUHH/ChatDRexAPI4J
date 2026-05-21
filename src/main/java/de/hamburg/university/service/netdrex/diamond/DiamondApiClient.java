package de.hamburg.university.service.netdrex.diamond;


import de.hamburg.university.service.netdrex.NetdrexJobApi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;


@Path("diamond")
@RegisterRestClient(configKey = "netdrex-client")
public interface DiamondApiClient extends NetdrexJobApi<SeedPayloadDTO, DiamondStatusResponseDTO> {

    @POST
    @Path("submit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> submit(SeedPayloadDTO payload);

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<DiamondStatusResponseDTO> status(@QueryParam("uid") String uid);
}
