package com.sismics.music.core.service.albumart;

/**
 * Album art sizes.
 *
 * @author jtremeaux
 */
public enum AlbumArtSize {
    SMALL(330),

    LARGE(768);

    private int size;

    AlbumArtSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
