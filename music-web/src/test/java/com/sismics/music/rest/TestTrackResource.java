package com.sismics.music.rest;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Files;
import com.sismics.util.filter.TokenBasedSecurityFilter;

/**
 * Exhaustive test of the track resource.
 * 
 * @author jtremeaux
 */
public class TestTrackResource extends BaseJerseyTest {
    /**
     * Test the track resource.
     *
     * @throws Exception
     */
    @Test
    public void testTrackResource() throws Exception {
        // Login users
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);

        // This test is destructive, copy the test music to a temporary directory
        Path sourceDir = Paths.get(getClass().getResource("/music/[A] Proxy - Coachella 2010 Day 01 Mixtape").toURI());
        File destDir = Files.createTempDir();
        destDir.deleteOnExit();
        for (File sourceFile : sourceDir.toFile().listFiles()) {
            File destFile = Paths.get(destDir.toString(), sourceFile.getName()).toFile();
            Files.copy(sourceFile, destFile);
            destFile.deleteOnExit();
        }
        
        // Admin adds a track to the collection
        JsonObject json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("location", destDir.toPath().toString())), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check that the albums are correctly added
        json = target().path("/album").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray albums = json.getJsonArray("albums");
        Assert.assertEquals(1, albums.size());
        JsonObject album0 = albums.getJsonObject(0);
        String album0Id = album0.getString("id");
        Assert.assertNotNull(album0Id);

        // Admin checks the tracks info
        json = target().path("/album/" + album0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray tracks = json.getJsonArray("tracks");
        Assert.assertEquals(2, tracks.size());
        JsonObject track0 = tracks.getJsonObject(0);
        String track0Id = track0.getString("id");
        Assert.assertFalse(track0.getBoolean("liked"));

        // Get an track by its ID.
        target().path("/track/" + track0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get();

        // Admin likes the track
        json = target().path("/track/" + track0Id + "/like").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Admin checks the tracks info
        json = target().path("/album/" + album0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertEquals(2, tracks.size());
        track0 = tracks.getJsonObject(0);
        Assert.assertTrue(track0.getBoolean("liked"));

        // Admin unlikes the track
        json = target().path("/track/" + track0Id + "/like").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .delete(JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Admin checks the tracks info
        json = target().path("/album/" + album0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertEquals(2, tracks.size());
        track0 = tracks.getJsonObject(0);
        Assert.assertFalse(track0.getBoolean("liked"));

        // Admin update a track info
        json = target().path("/track/"+ track0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("order", "1")
                        .param("title", "My fake title")
                        .param("album", "My fake album")
                        .param("artist", "My fake artist")
                        .param("album_artist", "My fake album artist")
                        .param("year", "2014")
                        .param("genre", "Pop")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Admin checks the tracks info
        json = target().path("/album/" + album0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.size());
        
        // Admin checks the new album
        json = target().path("/album").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        albums = json.getJsonArray("albums");
        Assert.assertEquals(2, albums.size());
        
        // Admin update a track info
        json = target().path("/track/"+ track0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("order", "1")
                        .param("title", "My fake title 2")
                        .param("album", "My fake album")
                        .param("artist", "My fake artist")
                        .param("album_artist", "My fake album artist")
                        .param("year", "2014")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Admin checks the albums
        json = target().path("/album").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        albums = json.getJsonArray("albums");
        Assert.assertEquals(2, albums.size());
    }
}
