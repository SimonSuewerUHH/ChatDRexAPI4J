package de.hamburg.university.service.netdrex;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;


@RegisterRestClient(configKey = "netdrex-client")
public interface NetdrexApiClient {

    @GET
    @Path("/get_by_id/{nodeCollection}")
    List<NetdrexAPIInfoDTO> getById(@PathParam("nodeCollection") String nodeCollection, @QueryParam("q") List<String> ids);
}