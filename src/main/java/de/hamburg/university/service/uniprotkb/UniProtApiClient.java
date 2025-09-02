package de.hamburg.university.service.uniprotkb;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "uniprot-client")
@Path("/uniprotkb")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UniProtApiClient {

    @GET
    @Path("/{acc}.json")
    UniProtEntryDTO getEntry(@PathParam("acc") String accession);
}
