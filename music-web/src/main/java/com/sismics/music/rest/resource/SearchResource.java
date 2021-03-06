package com.sismics.music.rest.resource;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sismics.music.core.dao.dbi.AlbumDao;
import com.sismics.music.core.dao.dbi.ArtistDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.criteria.AlbumCriteria;
import com.sismics.music.core.dao.dbi.criteria.ArtistCriteria;
import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
import com.sismics.music.core.dao.dbi.dto.AlbumDto;
import com.sismics.music.core.dao.dbi.dto.ArtistDto;
import com.sismics.music.core.dao.dbi.dto.TrackDto;
import com.sismics.music.core.util.dbi.PaginatedList;
import com.sismics.music.core.util.dbi.PaginatedLists;
import com.sismics.music.rest.util.JsonUtil;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

/**
 * Search REST resources.
 * 
 * @author jtremeaux
 */
@Path("/search")
public class SearchResource extends BaseResource {
    /**
     * Run a full text search.
     *
     * @param query Search query
     * @param limit Page limit
     * @param offset Page offset
     * @return Response
     */
    @GET
    @Path("{query: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @PathParam("query") String query,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ValidationUtil.validateRequired(query, "query");

        // Search tracks
        PaginatedList<TrackDto> paginatedList = PaginatedLists.create(limit, offset);
        TrackDao trackDao = new TrackDao();
        trackDao.findByCriteria(new TrackCriteria().setUserId(principal.getId()).setTitleLike(query), paginatedList);

        JsonArrayBuilder tracks = Json.createArrayBuilder();
        int i = 1;
        JsonObjectBuilder response = Json.createObjectBuilder();
        for (TrackDto trackDto : paginatedList.getResultList()) {
            tracks.add(Json.createObjectBuilder()
                    .add("order", i++)    // TODO use order from track
                    .add("id", trackDto.getId())
                    .add("title", trackDto.getTitle())
                    .add("year", JsonUtil.nullable(trackDto.getYear()))
                    .add("genre", JsonUtil.nullable(trackDto.getGenre()))
                    .add("length", trackDto.getLength())
                    .add("bitrate", trackDto.getBitrate())
                    .add("vbr", trackDto.isVbr())
                    .add("format", trackDto.getFormat())
                    .add("play_count", trackDto.getUserTrackPlayCount())
                    .add("liked", trackDto.isUserTrackLike())
                    .add("album", Json.createObjectBuilder()
                            .add("id", trackDto.getAlbumId())
                            .add("name", trackDto.getAlbumName())
                            .add("albumart", trackDto.getAlbumArt() != null))
                    .add("artist", Json.createObjectBuilder()
                            .add("id", trackDto.getArtistId())
                            .add("name", trackDto.getArtistName())));
        }
        response.add("tracks", tracks);

        // Search albums
        AlbumDao albumDao = new AlbumDao();
        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria().setNameLike(query));

        JsonArrayBuilder albums = Json.createArrayBuilder();
        for (AlbumDto album : albumList) {
            albums.add(Json.createObjectBuilder()
                    .add("id", album.getId())
                    .add("name", album.getName())
                    .add("albumart", album.getAlbumArt() != null)
                    .add("artist", Json.createObjectBuilder()
                            .add("id", album.getArtistId())
                            .add("name", album.getArtistName())));
        }
        response.add("albums", albums);
        
        // Search artists
        ArtistDao artistDao = new ArtistDao();
        List<ArtistDto> artistList = artistDao.findByCriteria(new ArtistCriteria().setNameLike(query));

        JsonArrayBuilder artists = Json.createArrayBuilder();
        for (ArtistDto artist : artistList) {
            artists.add(Json.createObjectBuilder()
                    .add("id", artist.getId())
                    .add("name", artist.getName()));
        }
        response.add("artists", artists);
        
        return Response.ok().entity(response.build()).build();
    }
}
