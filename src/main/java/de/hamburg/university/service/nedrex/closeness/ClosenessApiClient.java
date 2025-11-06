package de.hamburg.university.service.nedrex.closeness;


import de.hamburg.university.service.nedrex.NeDRexJobApi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;

@Path("/closeness")
@RegisterRestClient(configKey = "nedrex-client")
public interface ClosenessApiClient extends NeDRexJobApi<ClosenessSeedPayloadDTO, ClosenessStatusResponseDTO> {

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
