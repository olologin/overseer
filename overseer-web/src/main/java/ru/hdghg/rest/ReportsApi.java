package ru.hdghg.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hdghg.service.Storage;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Path("/reports-api")
@Consumes({ "application/json" })
@Produces({ "application/json" })
public class ReportsApi {
    private static final Logger log = LoggerFactory.getLogger(ReportsApi.class);

    @Inject
    private Storage storage;

    @GET
    @Path("/jids")
    public Response getJids() {
        log.info("Jids are requested");
        return wrapCollection(storage.allJids());
    }

    private <T> Response wrapCollection(Collection<T> result) {
        GenericEntity<Collection<T>> response = new GenericEntity<Collection<T>>(result) {};
        return Response.ok(response).build();
    }
}
