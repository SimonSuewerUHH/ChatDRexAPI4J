package de.hamburg.university.service.mygene;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/v3/query")
@RegisterRestClient(configKey = "mygene-client")
public interface MyGeneClient {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    MyGeneResponseDTO query(@QueryParam("q") String query,
                            @QueryParam("fields") String fields,
                            @QueryParam("species") String species);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    MyGeneGoHitDTO query(
            @QueryParam("q") String query,
            @QueryParam("fields") String fields,
            @QueryParam("scopes") String scopes,
            @QueryParam("size") Integer size
    );
}
