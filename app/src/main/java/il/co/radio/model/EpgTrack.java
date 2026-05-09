package il.co.radio.model;

public class EpgTrack {

    public String name;
    public String artist;
    public String desc;
    public String image;
    public String startTime;
    public String duration;

    public boolean hasImage() {
        return image != null && !image.trim().isEmpty();
    }

    public String getDisplayName() {
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        if (artist != null && !artist.trim().isEmpty()) {
            return artist.trim();
        }
        return "";
    }
}
