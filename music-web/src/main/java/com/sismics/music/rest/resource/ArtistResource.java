package com.sismics.music.rest.resource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.music.core.dao.dbi.AlbumDao;
import com.sismics.music.core.dao.dbi.ArtistDao;
import com.sismics.music.core.dao.dbi.criteria.ArtistCriteria;
import com.sismics.music.core.dao.dbi.dto.ArtistDto;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Album;
import com.sismics.music.core.model.dbi.Artist;
import com.sismics.music.core.service.albumart.AlbumArtService;
import com.sismics.music.core.service.albumart.AlbumArtSize;
import com.sismics.music.core.util.ImageUtil;
import com.sismics.rest.exception.ForbiddenClientException;

/**
 * Artist REST resources.
 * 
 * @author bgamard
 */
@Path("/artist")
public class ArtistResource extends BaseResource {
    /**
     * Returns all active artists.
     *
     * @return Response
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ArtistDao artistDao = new ArtistDao();
        List<ArtistDto> artistList = artistDao.findByCriteria(new ArtistCriteria());

        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<JSONObject>();
        for (ArtistDto artist : artistList) {
            JSONObject artistJson = new JSONObject();
            artistJson.put("id", artist.getId());
            artistJson.put("name", artist.getName());
            items.add(artistJson);
        }
        response.put("artists", items);

        return Response.ok().entity(response).build();
    }
    
    /**
     * Returns an artist cover.
     *
     * @param id Artist ID
     * @param size Cover size
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}/artistart/{size: [a-z]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response albumart(
            @PathParam("id") String id,
            @PathParam("size") String size) throws Exception {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the artist
        ArtistDao artistDao = new ArtistDao();
        Artist artist = artistDao.getActiveById(id);
        if (artist == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Get the artist art size
        AlbumArtSize artistArtSize = null;
        try {
            artistArtSize = AlbumArtSize.valueOf(size.toUpperCase());
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Get artist's album arts
        AlbumDao albumDao = new AlbumDao();
        final AlbumArtService albumArtService = AppContext.getInstance().getAlbumArtService();
        List<Album> albumList = albumDao.getActiveByArtistId(id);
        List<BufferedImage> imageList = new ArrayList<>();
        for (Album album : albumList) {
            if (album.getAlbumArt() != null) {
                File file = albumArtService.getAlbumArtFile(album.getAlbumArt(), artistArtSize);
                if (file.exists() && file.canRead()) {
                    imageList.add(ImageIO.read(file));
                }
            }
        }
        
        // Write to JPEG
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(ImageUtil.makeMosaic(imageList, artistArtSize.getSize()), "jpeg", outputStream);
        byte[] imageData = outputStream.toByteArray();
        
        return Response.ok(imageData, "image/jpeg")
                .header("Expires", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date().getTime() + 3600000 * 24 * 7))
                .build();
    }
}
