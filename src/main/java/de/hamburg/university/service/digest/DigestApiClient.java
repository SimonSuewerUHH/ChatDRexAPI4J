package de.hamburg.university.service.digest;


import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;
import java.util.concurrent.CompletionStage;

@RegisterRestClient(configKey = "digest-client")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface DigestApiClient {

    @POST
    @Path("/set")
    Uni<DigestTaskResponseDTO> submitSet(DigestSubmitRequestDTO payload);

    @POST
    @Path("/subnetwork")
    Uni<DigestTaskResponseDTO> submitSubnetwork(DigestSubmitRequestDTO payload);

    @GET
    @Path("/status")
    CompletionStage<DigestStatusResponseDTO> status(@QueryParam("task") String taskId);

    @GET
    @Path("/result")
    Uni<DigestResultResponseDTO> result(@QueryParam("task") String taskId);

    @GET
    @Path("/result_file_list")
    List<DigestFileResultResponseDTO> resultFileList(@QueryParam("task") String taskId);
}
