package com.sismics.music.rest.resource;

import javax.ws.rs.Path;

/**
 * Track REST resources.
 * 
 * @author jtremeaux
 */
@Path("/track")
public class TrackResource extends BaseResource {
//    /**
//     * Returns a track stream.
//     *
//     * @param id Track ID
//     * @return Response
//     * @throws Exception 
//     */
//    @GET
//    @Path("{id: [a-z0-9\\-]+}")
//    @Produces("audio/mpeg")
//    public Response stream(
//            @HeaderParam("Range") String range,
//            @PathParam("id") String id) throws Exception {
//        if (!authenticate()) {
//            throw new ForbiddenClientException();
//        }
//
//        TrackDao trackDao = new TrackDao();
//        Track track = trackDao.getActiveById(id);
//        TransactionUtil.commit();
//        if (track == null) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//
//        final File file = new File(track.getFileName());
//        if (!file.exists() || !file.canRead()) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//
//        // TODO Transcode mp3 as well
//        final TranscoderService transcoderService = AppContext.getInstance().getTranscoderService();
//        if (!"mp3".equals(track.getFormat())) {
//            int seek = 0;
//            int from = 0;
//            int to = 0;
//            if (range != null) {
//                // Range requested, send a 206 partial content
//                String[] ranges = range.split("=")[1].split("-");
//                from = Integer.parseInt(ranges[0]);
//                seek = from / (128 * 1000 / 8);
//            }
//            int fileSize = track.getLength() * 128 * 1000 / 8;
//            InputStream is = transcoderService.getTranscodedInputStream(track, seek);
//            Response.ResponseBuilder response = Response.ok(is);
//            if (range != null) {
//                response = response.status(206);
//                final String responseRange = String.format("bytes %d-%d/%d", from, to, fileSize);
//                response.header("Accept-Ranges", "bytes");
//                response.header("Content-Range", responseRange);
//            }
//            response.header(HttpHeaders.CONTENT_LENGTH, fileSize);
//            response.header(HttpHeaders.LAST_MODIFIED, new Date(file.lastModified()));
//            return response.build();
//        } else {
//            // Range not requested, serve the whole file
//            if (range == null) {
//                StreamingOutput streamer = new StreamingOutput() {
//                    @Override
//                    public void write(final OutputStream output) throws IOException, WebApplicationException {
//                        @SuppressWarnings("resource")
//                        final FileChannel inputChannel = new FileInputStream(file).getChannel();
//                        final WritableByteChannel outputChannel = Channels.newChannel(output);
//                        try {
//                            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
//                        } finally {
//                            // Closing the channels
//                            inputChannel.close();
//                            outputChannel.close();
//                        }
//                    }
//                };
//                return Response.ok(streamer).status(200).header(HttpHeaders.CONTENT_LENGTH, file.length()).build();
//            }
//
//            // Range requested, send a 206 partial content
//            String[] ranges = range.split("=")[1].split("-");
//            final int from = Integer.parseInt(ranges[0]);
//
//            // Don't chunk the file, send content to the end
//            final int to = (int) (file.length() - 1);
//
//            final String responseRange = String.format("bytes %d-%d/%d", from, to, file.length());
//            final RandomAccessFile raf = new RandomAccessFile(file, "r");
//            raf.seek(from);
//
//            final int len = to - from + 1;
//            final MediaStreamer streamer = new MediaStreamer(len, raf);
//            return Response.ok(streamer).status(206)
//                    .header("Accept-Ranges", "bytes")
//                    .header("Content-Range", responseRange)
//                    .header(HttpHeaders.CONTENT_LENGTH, streamer.getLength())
//                    .header(HttpHeaders.LAST_MODIFIED, new Date(file.lastModified()))
//                    .build();
//        }
//    }
//
//    /**
//     * Like a track.
//     *
//     * @return Response
//     * @throws JSONException
//     */
//    @POST
//    @Path("{id: [a-z0-9\\-]+}/like")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response like(
//            @PathParam("id") String id) throws JSONException {
//        if (!authenticate()) {
//            throw new ForbiddenClientException();
//        }
//
//        TrackDao trackDao = new TrackDao();
//        Track track = trackDao.getActiveById(id);
//        if (track == null) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//
//        // Like the track locally
//        UserTrackDao userTrackDao = new UserTrackDao();
//        userTrackDao.like(principal.getId(), track.getId());
//
//        // Love the track on Last.fm
//        final User user = new UserDao().getActiveById(principal.getId());
//        if (user != null && user.getLastFmSessionToken() != null) {
//            final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
//            lastFmService.loveTrack(user, track);
//        }
//
//        // Always return OK
//        JSONObject response = new JSONObject();
//        response.put("status", "ok");
//        return Response.ok().entity(response).build();
//    }
//
//    /**
//     * Unlike a track.
//     *
//     * @return Response
//     * @throws JSONException
//     */
//    @DELETE
//    @Path("{id: [a-z0-9\\-]+}/like")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response unlike(
//            @PathParam("id") String id) throws JSONException {
//        if (!authenticate()) {
//            throw new ForbiddenClientException();
//        }
//
//        TrackDao trackDao = new TrackDao();
//        Track track = trackDao.getActiveById(id);
//        if (track == null) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//
//        // Unlike the track locally
//        UserTrackDao userTrackDao = new UserTrackDao();
//        userTrackDao.unlike(principal.getId(), track.getId());
//
//        // Unove the track on Last.fm
//        final User user = new UserDao().getActiveById(principal.getId());
//        if (user != null && user.getLastFmSessionToken() != null) {
//            final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
//            lastFmService.unloveTrack(user, track);
//        }
//
//        // Always return OK
//        JSONObject response = new JSONObject();
//        response.put("status", "ok");
//        return Response.ok().entity(response).build();
//    }
//    
//    @POST
//    @Path("{id: [a-z0-9\\-]+}")
//    public Response update(
//            @PathParam("id") String id,
//            @FormParam("order") String orderStr,
//            @FormParam("title") String title,
//            @FormParam("album") String album,
//            @FormParam("artist") String artist,
//            @FormParam("album_artist") String albumArtist,
//            @FormParam("year") String yearStr,
//            @FormParam("genre") String genre) throws JSONException {
//        if (!authenticate()) {
//            throw new ForbiddenClientException();
//        }
//
//        TrackDao trackDao = new TrackDao();
//        Track track = trackDao.getActiveById(id);
//        if (track == null) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//        
//        // TODO Update track in database/tags.
//        
//        // Always return OK
//        JSONObject response = new JSONObject();
//        response.put("status", "ok");
//        return Response.ok().entity(response).build();
//    }
}
