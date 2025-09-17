package de.hamburg.university.service.research.pubmed;

import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

//https://www.ncbi.nlm.nih.gov/books/NBK25499/
@RegisterRestClient(configKey = "pubmed-client")
@Path("/entrez/eutils")
public interface PubmedService {

    @GET
    @Path("esearch.fcgi")
    @Produces(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "db", value = "pubmed")
    @ClientQueryParam(name = "retmode", value = "json")
    @ClientQueryParam(name = "datetype", value = "pdat")
    PubMedSearchResultDTO bulkSearch(
            @HeaderParam("x-api-key") String apiKey,
            @QueryParam("term") String query,
            @QueryParam("mindate") String mindate,
            @QueryParam("maxdate") String maxdate,
            @QueryParam("retmax") int maxResults,
            @QueryParam("retstart") int start);

    @GET
    @Path("esearch.fcgi")
    @Produces(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "db", value = "pubmed")
    @ClientQueryParam(name = "retmode", value = "json")
    PubMedSearchResultDTO bulkSearch(
            @HeaderParam("x-api-key") String apiKey,
            @QueryParam("term") String query,
            @QueryParam("retmax") int maxResults,
            @QueryParam("retstart") int start);


    @GET
    @Path("esearch.fcgi")
    @Produces(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "db", value = "pubmed")
    @ClientQueryParam(name = "retmode", value = "json")
    @ClientQueryParam(name = "datetype", value = "pdat")
    PubMedSearchResultDTO search(
            @HeaderParam("x-api-key") String apiKey,
            @QueryParam("term") String query,
            @QueryParam("mindate") String mindate,
            @QueryParam("maxdate") String maxdate);

    @GET
    @Path("esearch.fcgi")
    @Produces(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "db", value = "pubmed")
    @ClientQueryParam(name = "retmode", value = "json")
    PubMedSearchResultDTO search(
            @HeaderParam("x-api-key") String apiKey,
            @QueryParam("term") String query);


    @GET
    @Path("esearch.fcgi")
    @Produces(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "db", value = "pubmed")
    @ClientQueryParam(name = "retmode", value = "json")
    PubMedSearchResultDTO search(
            @QueryParam("term") String query);

    @GET
    @Path("efetch.fcgi")
    @Produces("text/xml;charset=UTF-8")
    @Consumes("text/xml;charset=UTF-8")
    @ClientQueryParam(name = "db", value = "pubmed")
    String fetchArticlesAsString(
            @HeaderParam("x-api-key") String apiKey,
            @QueryParam("id") String ids);
}
