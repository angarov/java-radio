package il.co.radio.model;

import java.util.List;

public class Station {

    public List<String> feed;

    public List<String> img;
    public List<String> name;
    public List<String> nowplaying;

    private String first(List<String> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    public String getStreamUrl()    { return first(feed); }
    public String getImageUrl()     { return first(img); }
    public String getNowPlayingUrl(){ return first(nowplaying); }

    public String getName() {
        String n = first(name);
        return n != null ? n : "";
    }
}
