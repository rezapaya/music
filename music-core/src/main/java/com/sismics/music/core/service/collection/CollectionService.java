package com.sismics.music.core.service.collection;

import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.music.core.dao.dbi.AlbumDao;
import com.sismics.music.core.dao.dbi.ArtistDao;
import com.sismics.music.core.dao.dbi.DirectoryDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.criteria.AlbumCriteria;
import com.sismics.music.core.dao.dbi.dto.AlbumDto;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Album;
import com.sismics.music.core.model.dbi.Artist;
import com.sismics.music.core.model.dbi.Directory;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.service.albumart.AlbumArtImporter;
import com.sismics.music.core.util.TransactionUtil;

/**
 * Collection service.
 *
 * @author jtremeaux
 */
public class CollectionService extends AbstractScheduledService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(CollectionService.class);

    public CollectionService() {
    }

    @Override
    protected void startUp() {
    }

    @Override
    protected void shutDown() {
    }

    @Override
    protected void runOneIteration() throws Exception {
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                // NOP
            }
        });
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 24, TimeUnit.HOURS);
    }

    /**
     * Add a directory to the index / update existing index.
     *
     * @param directory Directory to index
     */
    public void addDirectoryToIndex(Directory directory) {
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Adding directory {0} to index", directory.getLocation()));
        }
        // Index the directory recursively
        new CollectionVisitor(directory).index();

        // Delete all artists that don't have any album or track
        ArtistDao artistDao = new ArtistDao();
        artistDao.deleteEmptyArtist();

        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Done adding directory {0} to index", directory.getLocation()));
        }
    }

    /**
     * Remove a directory from the index.
     *
     * @param directory Directory to index
     */
    public void removeDirectoryFromIndex(Directory directory) {
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Removing directory {0} from index", directory.getLocation()));
        }
        // Delete all albums from this directory
        AlbumDao albumDao = new AlbumDao();
        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria().setDirectoryId(directory.getId()));
        for (AlbumDto albumDto : albumList) {
            albumDao.delete(albumDto.getId());
        }

        // Delete all artists that don't have any album or track
        ArtistDao artistDao = new ArtistDao();
        artistDao.deleteEmptyArtist();

        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Done removing directory {0} from index", directory.getLocation()));
        }
    }

    /**
     * Add / update a media file to the index.
     *
     * @param rootDirectory Directory to index
     * @param file File to add
     */
    public void indexFile(Directory rootDirectory, Path file) {
        Stopwatch stopWatch = Stopwatch.createStarted();
        try {
            TrackDao trackDao = new TrackDao();
            Track track = trackDao.getActiveByDirectoryAndFilename(rootDirectory.getId(), file.toAbsolutePath().toString());
            if (track != null) {
                readTrackMetadata(rootDirectory, file, track);
            } else {
                track = new Track();
                track.setFileName(file.toAbsolutePath().toString());

                readTrackMetadata(rootDirectory, file, track);
                trackDao.create(track);
            }
        } catch (Exception e) {
            log.error("Error extracting metadata from file: " + file, e);
        }
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("File {0} indexed in {1}", file, stopWatch));
        }
    }

    /**
     * Read metadata from a media file into the Track.
     *
     * @param rootDirectory Root directory to index
     * @param file Media file to read from
     * @param track Track entity (updated)
     */
    public void readTrackMetadata(Directory rootDirectory, Path file, Track track) throws Exception {
        AudioFile audioFile = AudioFileIO.read(file.toFile());
        Tag tag = audioFile.getTag();
        // TODO deal with empty tags
        AudioHeader header = audioFile.getAudioHeader();

        track.setLength(header.getTrackLength());
        track.setBitrate(header.getSampleRateAsNumber());
        track.setFormat(StringUtils.abbreviate(header.getEncodingType(), 50));
        track.setVbr(header.isVariableBitRate());

        String year = tag.getFirst(FieldKey.YEAR);
        if (!Strings.isNullOrEmpty(year)) {
            try {
                track.setYear(Integer.valueOf(year));
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        track.setTitle(StringUtils.abbreviate(tag.getFirst(FieldKey.TITLE), 2000));
        String artistName = StringUtils.abbreviate(tag.getFirst(FieldKey.ARTIST), 1000);
        ArtistDao artistDao = new ArtistDao();
        Artist artist = artistDao.getActiveByName(artistName);
        if (artist == null) {
            artist = new Artist();
            artist.setName(artistName);
            artistDao.create(artist);
        }
        track.setArtistId(artist.getId());

        String albumArtistName = StringUtils.abbreviate(tag.getFirst(FieldKey.ALBUM_ARTIST), 1000);
        Artist albumArtist = null;
        if (!Strings.isNullOrEmpty(albumArtistName)) {
            albumArtist = artistDao.getActiveByName(albumArtistName);
            if (albumArtist == null) {
                albumArtist = new Artist();
                albumArtist.setName(albumArtistName);
                artistDao.create(albumArtist);
            }
        } else {
            albumArtist = artist;
        }

        String albumName = StringUtils.abbreviate(tag.getFirst(FieldKey.ALBUM), 1000);
        AlbumDao albumDao = new AlbumDao();
        Album album = albumDao.getActiveByArtistIdAndName(albumArtist.getId(), albumName);
        if (album == null) {
            // Import album art
            AlbumArtImporter albumArtImporter = new AlbumArtImporter();
            File albumArtFile = albumArtImporter.scanDirectory(file.getParent());

            album = new Album();
            album.setArtistId(albumArtist.getId());
            album.setDirectoryId(rootDirectory.getId());
            album.setName(albumName);
            if (albumArtFile != null) {
                String albumArtId = AppContext.getInstance().getAlbumArtService().importAlbumArt(albumArtFile);
                album.setAlbumArt(albumArtId);
            }
            albumDao.create(album);
        }
        track.setAlbumId(album.getId());
    }

    /**
     * Reindex the whole collection.
     */
    public void reindex() {
        DirectoryDao directoryDao = new DirectoryDao();
        List<Directory> directoryList = directoryDao.findAllEnabled();
        for (Directory directory : directoryList) {
            addDirectoryToIndex(directory);
        }
    }

    /**
     * Update the album scores.
     * TODO implement a more elaborated scoring function
     */
    public void updateScore() {
//        AlbumDao albumDao = new AlbumDao();
//        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria());
//        TODO implement scoring
//        for (AlbumDto albumDto : albumList) {
//            Integer score = albumDao.getFavoriteCountByAlbum(albumDto.getId());
//
//            Album album = new Album();
//            album.setId(albumDto.getId());
//            album.setScore(score);
//
//            albumDao.updateScore(album);
//        }
    }
}
