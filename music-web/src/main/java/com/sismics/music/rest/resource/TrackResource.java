package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.jpa.TrackDao;
import com.sismics.music.core.model.jpa.Track;
import com.sismics.music.core.util.TransactionUtil;
import com.sismics.rest.exception.ForbiddenClientException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * Track REST resources.
 * 
 * @author jtremeaux
 */
@Path("/track")
public class TrackResource extends BaseResource {
    /**
     * Returns a track stream.
     *
     * @param id Track ID
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stream(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        TrackDao trackDao = new TrackDao();
        Track track = trackDao.getActiveById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        File file = new File(track.getFileName());
        if (!file.exists() || !file.canRead()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        TransactionUtil.commit();

        return Response.ok(file, "audio/mpeg").build();
    }

    /**
     * Like a track.
     *
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/like")
    @Produces(MediaType.APPLICATION_JSON)
    public Response like(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        TrackDao trackDao = new TrackDao();
        Track track = trackDao.getActiveById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // TODO like track

        // Always return OK
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Unlike a track.
     *
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}/like")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unlike(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        TrackDao trackDao = new TrackDao();
        Track track = trackDao.getActiveById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // TODO unlike track

        // Always return OK
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
