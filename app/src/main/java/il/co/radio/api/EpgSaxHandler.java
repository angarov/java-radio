package il.co.radio.api;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

import il.co.radio.model.EpgResponse;
import il.co.radio.model.EpgTrack;

public class EpgSaxHandler extends DefaultHandler {

    private final List<EpgTrack> tracks = new ArrayList<>();
    private EpgTrack currentTrack;
    private final StringBuilder textContent = new StringBuilder();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (localName.equals("track")) {
            currentTrack = new EpgTrack();
        }
        textContent.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        textContent.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (currentTrack == null) {
            return;
        }

        switch (localName) {
            case "name":
                currentTrack.name = textContent.toString();
                break;
            case "artist":
                currentTrack.artist = textContent.toString();
                break;
            case "image":
                currentTrack.image = textContent.toString();
                break;
            case "startTime":
                currentTrack.startTime = textContent.toString();
                break;
            case "duration":
                currentTrack.duration = textContent.toString();
                break;
            case "track":
                tracks.add(currentTrack);
                currentTrack = null;
                break;
        }
    }

    public EpgResponse getResult() {
        EpgResponse response = new EpgResponse();
        response.tracks = tracks;
        return response;
    }
}
