package il.co.radio.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import il.co.radio.api.ApiCallback;
import il.co.radio.api.ApiClient;
import il.co.radio.db.RadioDatabase;
import il.co.radio.db.StationDao;
import il.co.radio.db.StationEntity;
import il.co.radio.model.Station;
import il.co.radio.model.StationsResponse;

public class StationRepository {

    private static StationRepository instance;
    private final ApiClient apiClient;
    private final StationDao stationDao;
    private final ExecutorService dbExecutor;
    private final Handler mainHandler;

    private StationRepository(Context context) {
        apiClient = ApiClient.getInstance();
        stationDao = RadioDatabase.getInstance(context).stationDao();
        dbExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized StationRepository getInstance(Context context) {
        if (instance == null) {
            instance = new StationRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void getStations(String url, ApiCallback<StationsResponse> callback) {
        apiClient.getStations(url, new ApiCallback<StationsResponse>() {
            @Override
            public void onSuccess(StationsResponse response) {
                dbExecutor.execute(() -> cacheStations(url, response.data));
                callback.onSuccess(response);
            }

            @Override
            public void onFailure(Exception error) {
                dbExecutor.execute(() -> {
                    List<Station> cached = loadCachedStations(url);
                    mainHandler.post(() -> {
                        if (cached != null && !cached.isEmpty()) {
                            StationsResponse response = new StationsResponse();
                            response.data = cached;
                            callback.onSuccess(response);
                        } else {
                            callback.onFailure(error);
                        }
                    });
                });
            }
        });
    }

    private void cacheStations(String url, List<Station> stations) {
        if (stations == null) return;
        List<StationEntity> entities = new ArrayList<>(stations.size());
        for (int i = 0; i < stations.size(); i++) {
            entities.add(toEntity(stations.get(i), url, i));
        }
        stationDao.replaceStations(url, entities);
    }

    private List<Station> loadCachedStations(String url) {
        List<StationEntity> entities = stationDao.getStationsBySource(url);
        if (entities == null || entities.isEmpty()) return null;
        List<Station> stations = new ArrayList<>(entities.size());
        for (StationEntity entity : entities) {
            stations.add(toStation(entity));
        }
        return stations;
    }

    private StationEntity toEntity(Station station, String sourceUrl, int position) {
        StationEntity entity = new StationEntity();
        entity.sourceUrl = sourceUrl;
        entity.position = position;
        entity.feed = station.feed;
        entity.img = station.img;
        entity.name = station.name;
        entity.nowplaying = station.nowplaying;
        entity.maincolor = station.maincolor;
        return entity;
    }

    private Station toStation(StationEntity entity) {
        Station station = new Station();
        station.feed = entity.feed;
        station.img = entity.img;
        station.name = entity.name;
        station.nowplaying = entity.nowplaying;
        station.maincolor = entity.maincolor;
        return station;
    }
}
