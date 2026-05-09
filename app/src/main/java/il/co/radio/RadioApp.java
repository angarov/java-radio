package il.co.radio;

import android.app.Application;

import il.co.radio.api.ApiClient;

public class RadioApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.getInstance();
    }
}
