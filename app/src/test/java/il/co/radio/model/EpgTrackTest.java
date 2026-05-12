package il.co.radio.model;

import static org.junit.Assert.*;
import org.junit.Test;

public class EpgTrackTest {

    @Test
    public void hasImage_nullImage_returnsFalse() {
        EpgTrack track = new EpgTrack();
        track.image = null;
        assertFalse(track.hasImage());
    }

    @Test
    public void hasImage_emptyImage_returnsFalse() {
        EpgTrack track = new EpgTrack();
        track.image = "";
        assertFalse(track.hasImage());
    }

    @Test
    public void hasImage_whitespaceImage_returnsFalse() {
        EpgTrack track = new EpgTrack();
        track.image = "   ";
        assertFalse(track.hasImage());
    }

    @Test
    public void hasImage_validUrl_returnsTrue() {
        EpgTrack track = new EpgTrack();
        track.image = "https://example.com/art.jpg";
        assertTrue(track.hasImage());
    }

    @Test
    public void getDisplayName_returnsName_whenPresent() {
        EpgTrack track = new EpgTrack();
        track.name = "  Song Title  ";
        track.artist = "Artist";
        assertEquals("Song Title", track.getDisplayName());
    }

    @Test
    public void getDisplayName_fallsBackToArtist_whenNameBlank() {
        EpgTrack track = new EpgTrack();
        track.name = "   ";
        track.artist = " Some Artist ";
        assertEquals("Some Artist", track.getDisplayName());
    }

    @Test
    public void getDisplayName_returnsEmpty_whenBothNull() {
        EpgTrack track = new EpgTrack();
        track.name = null;
        track.artist = null;
        assertEquals("", track.getDisplayName());
    }
}
