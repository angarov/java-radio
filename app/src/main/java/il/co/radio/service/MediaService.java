package il.co.radio.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;

import il.co.radio.R;
import il.co.radio.ui.MainActivity;

public class MediaService extends Service {

    public static final String ACTION_PLAY = "il.co.radio.ACTION_PLAY";
    public static final String ACTION_STOP = "il.co.radio.ACTION_STOP";
    public static final String EXTRA_STREAM_URL = "stream_url";
    public static final String EXTRA_STATION_NAME = "station_name";

    public static final String BROADCAST_PLAYBACK_STATE = "il.co.radio.PLAYBACK_STATE";
    public static final String EXTRA_IS_PLAYING = "is_playing";
    public static final String EXTRA_IS_BUFFERING = "is_buffering";

    private static final String CHANNEL_ID = "radio_playback";
    private static final int NOTIFICATION_ID = 1;

    private ExoPlayer player;
    private String currentStationName = "";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        DefaultHttpDataSource.Factory httpFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent("RadioApp/1.0 (Android)")
                .setConnectTimeoutMs(15_000)
                .setReadTimeoutMs(15_000)
                .setAllowCrossProtocolRedirects(true);

        DefaultDataSource.Factory dataSourceFactory =
                new DefaultDataSource.Factory(this, httpFactory);

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .build();
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                broadcastState();
                updateNotification();
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                broadcastState();
                updateNotification();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String action = intent.getAction();
        if (ACTION_PLAY.equals(action)) {
            String streamUrl = intent.getStringExtra(EXTRA_STREAM_URL);
            currentStationName = intent.getStringExtra(EXTRA_STATION_NAME);
            if (currentStationName == null) currentStationName = "";
            if (streamUrl != null) {
                play(streamUrl);
            }
        } else if (ACTION_STOP.equals(action)) {
            stop();
        }

        return START_NOT_STICKY;
    }

    private void play(String streamUrl) {
        player.stop();
        player.setMediaItem(buildMediaItem(streamUrl));
        player.prepare();
        player.setPlayWhenReady(true);
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    private MediaItem buildMediaItem(String streamUrl) {
        String mimeType = inferMimeType(streamUrl);
        if (mimeType != null) {
            return new MediaItem.Builder()
                    .setUri(streamUrl)
                    .setMimeType(mimeType)
                    .build();
        }
        return MediaItem.fromUri(streamUrl);
    }

    private String inferMimeType(String url) {
        if (url == null) return null;
        String path = url;
        int q = url.indexOf('?');
        if (q > 0) path = url.substring(0, q);
        int f = path.indexOf('#');
        if (f > 0) path = path.substring(0, f);
        path = path.toLowerCase();
        if (path.endsWith(".mp3")) return MimeTypes.AUDIO_MPEG;
        if (path.endsWith(".aac")) return MimeTypes.AUDIO_AAC;
        if (path.endsWith(".ogg") || path.endsWith(".oga")) return MimeTypes.AUDIO_OGG;
        return null;
    }

    private void stop() {
        player.stop();
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
        broadcastState();
    }

    public void togglePlayback() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    private void broadcastState() {
        Intent intent = new Intent(BROADCAST_PLAYBACK_STATE);
        intent.putExtra(EXTRA_IS_PLAYING, player.isPlaying());
        intent.putExtra(EXTRA_IS_BUFFERING,
                player.getPlaybackState() == Player.STATE_BUFFERING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Radio playback controls");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        Intent tapIntent = new Intent(this, MainActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(currentStationName)
                .setContentText("Playing")
                .setSmallIcon(R.drawable.ic_radio)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void updateNotification() {
        if (player.isPlaying() || player.getPlaybackState() == Player.STATE_BUFFERING) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
