package il.co.radio.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import il.co.radio.model.EpgResponse;
import il.co.radio.model.Station;
import il.co.radio.model.StationsResponse;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final String STATIONS_URL = "https://digital.100fm.co.il/allfm/stations/api/local";
    private static final int CONNECT_TIMEOUT = 30_000;
    private static final int READ_TIMEOUT = 30_000;

    private static ApiClient instance;
    private final ExecutorService executor;
    private final Handler mainHandler;

    private ApiClient() {
        executor = Executors.newFixedThreadPool(2);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public void getStations(ApiCallback<StationsResponse> callback) {
        getStations(STATIONS_URL, callback);
    }

    public void getStations(String url, ApiCallback<StationsResponse> callback) {
        executor.execute(() -> {
            try {
                String json = httpGet(url);
                StationsResponse response = parseStationsJson(json);
                mainHandler.post(() -> callback.onSuccess(response));
            } catch (Exception e) {
                Log.e(TAG, "getStations failed", e);
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void getNowPlaying(String url, ApiCallback<EpgResponse> callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                conn = openConnection(url);
                int code = conn.getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP " + code);
                }
                InputStream stream = conn.getInputStream();
                EpgResponse response = parseEpgXml(stream);
                mainHandler.post(() -> callback.onSuccess(response));
            } catch (Exception e) {
                Log.e(TAG, "getNowPlaying failed", e);
                mainHandler.post(() -> callback.onFailure(e));
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private String httpGet(String urlString) throws Exception {
        HttpURLConnection conn = openConnection(urlString);
        try {
            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new Exception("HTTP " + code);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } finally {
            conn.disconnect();
        }
    }

    private HttpURLConnection openConnection(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        return conn;
    }

    private StationsResponse parseStationsJson(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        JSONArray dataArray = root.getJSONArray("data");

        List<Station> stations = new ArrayList<>();
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject obj = dataArray.getJSONObject(i);
            Station station = new Station();
            station.feed = jsonArrayToList(obj.optJSONArray("Feed"));
            station.img = jsonArrayToList(obj.optJSONArray("img"));
            station.name = jsonArrayToList(obj.optJSONArray("name"));
            station.nowplaying = jsonArrayToList(obj.optJSONArray("nowplaying"));
            station.maincolor = jsonArrayToList(obj.optJSONArray("maincolor"));
            station.voice = jsonArrayToList(obj.optJSONArray("voice"));
            station.info = jsonArrayToList(obj.optJSONArray("info"));
            stations.add(station);
        }

        StationsResponse response = new StationsResponse();
        response.data = stations;
        return response;
    }

    private List<String> jsonArrayToList(JSONArray array) {
        if (array == null) return null;
        List<String> list = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            list.add(array.optString(i));
        }
        return list;
    }

    private EpgResponse parseEpgXml(InputStream stream) throws Exception {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();

            EpgSaxHandler handler = new EpgSaxHandler();
            reader.setContentHandler(handler);
            reader.parse(new InputSource(stream));

            return handler.getResult();
        } finally {
            stream.close();
        }
    }
}
