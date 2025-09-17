package de.hamburg.university.service.nedrex.diamond;


import de.hamburg.university.service.nedrex.NeDRexJobApi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;


@Path("diamond")
@RegisterRestClient(configKey = "nedrex-client")
public interface DiamondApiClient extends NeDRexJobApi<SeedPayloadDTO, DiamondStatusResponseDTO> {

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
