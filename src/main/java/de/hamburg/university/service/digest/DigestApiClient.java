package de.hamburg.university.service.digest;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.Map;
import java.util.concurrent.CompletionStage;

@RegisterRestClient(configKey = "digest-client")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface DigestApiClient {

    @POST
    @Path("/set")
    DigestTaskResponseDTO submitSet(DigestSubmitRequestDTO payload);

    @POST
    @Path("/subnetwork")
    DigestTaskResponseDTO submitSubnetwork(DigestSubmitRequestDTO payload);

    @GET
    @Path("/status")
    CompletionStage<DigestStatusResponseDTO> status(@QueryParam("task") String taskId);

    @GET
    @Path("/result")
    DigestResultResponseDTO result(@QueryParam("task") String taskId);
}
