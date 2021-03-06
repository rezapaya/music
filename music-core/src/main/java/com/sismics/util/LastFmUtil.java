package com.sismics.util;

import de.umass.lastfm.*;

/**
 * Last.fm utilities.
 *
 * @author jtremeaux
 */
public class LastFmUtil {
    /**
     * Retrieves the loved tracks by a user.
     *
     * @param user The user name to fetch the loved tracks for.
     * @param page The page number to scan to
     * @param limit Limit (default 1000)
     * @param apiKey A Last.fm API key.
     * @return the loved tracks
     */
    public static PaginatedResult<Track> getLovedTracks(String user, int page, int limit, String apiKey) {
        Result result = Caller.getInstance().call("user.getLovedTracks", apiKey, "user", user, "page", String.valueOf(page), "limit", String.valueOf(limit));
        return ResponseBuilder.buildPaginatedResult(result, Track.class);
    }
}
