package de.hamburg.university.service.nedrex;


import de.hamburg.university.service.nedrex.kg.NeDRexSearchEmbeddingRequestDTO;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestStreamElementType;

import java.util.List;


@RegisterRestClient(configKey = "nedrex-client")
public interface NeDRexApiClient {

    @POST
    @Path("/find_by_ids/")
    @Produces(MediaType.APPLICATION_JSON)
    List<NeDRexAPIInfoDTO> getByIds(NeDRexAPIInfoRequestDTO request);

    @GET
    @Path("/get_by_id/{nodeCollection}/{q}")
    List<NeDRexAPIInfoDTO> getById(@PathParam("nodeCollection") String nodeCollection, @PathParam("q") String id);


    @POST
    @Path("open/embeddings/query")
    @Consumes(MediaType.WILDCARD)
    @ClientHeaderParam(name = "Accept", value = "application/json")
    Response queryEmbeddings(NeDRexSearchEmbeddingRequestDTO request);

    @GET
    @Path("open/neo4j/query")
    @Produces(MediaType.TEXT_PLAIN)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    Multi<String> streamQuery(@QueryParam("query") String cypher);

    @GET
    @Path("open/neo4j/query")
    @Produces(MediaType.TEXT_PLAIN)
    @ClientQueryParam(name = "stream", value = "false")
    String runQuery(@QueryParam("query") String cypher);

}