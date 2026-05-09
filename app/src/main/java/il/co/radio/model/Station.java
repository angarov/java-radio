package il.co.radio.model;

import java.util.List;

public class Station {

    public List<String> feed;

    public List<String> img;
    public List<String> name;
    public List<String> nowplaying;
    public List<String> maincolor;
    public List<String> voice;
    public List<String> info;

    public String getStreamUrl() {
        return feed != null && !feed.isEmpty() ? feed.get(0) : null;
    }

    public String getName() {
        return name != null && !name.isEmpty() ? name.get(0) : "";
    }

    public String getImageUrl() {
        return img != null && !img.isEmpty() ? img.get(0) : null;
    }

    public String getNowPlayingUrl() {
        return nowplaying != null && !nowplaying.isEmpty() ? nowplaying.get(0) : null;
    }

    public String getMainColor() {
        return maincolor != null && !maincolor.isEmpty() ? maincolor.get(0) : null;
    }
}
