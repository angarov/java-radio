package il.co.radio.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.tabs.TabLayoutMediator;
import com.bumptech.glide.Glide;

import il.co.radio.R;
import il.co.radio.api.ApiCallback;
import il.co.radio.api.ApiClient;
import il.co.radio.databinding.ActivityMainBinding;
import il.co.radio.model.EpgResponse;
import il.co.radio.model.EpgTrack;
import il.co.radio.model.Station;
import il.co.radio.service.MediaService;

public class MainActivity extends AppCompatActivity implements StationListFragment.OnStationSelectedListener {

    private ActivityMainBinding binding;
    private Station currentStation;
    private boolean isPlaying = false;

    private final Handler epgHandler = new Handler(Looper.getMainLooper());
    private final Runnable epgRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentStation != null) {
                fetchNowPlaying();
                epgHandler.postDelayed(this, 30000);
            }
        }
    };

    private final BroadcastReceiver playbackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isPlaying = intent.getBooleanExtra(MediaService.EXTRA_IS_PLAYING, false);
            boolean isBuffering = intent.getBooleanExtra(MediaService.EXTRA_IS_BUFFERING, false);
            updatePlayerUI(isPlaying, isBuffering);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int headerBasePaddingTop = binding.header.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(binding.header, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), insets.top + headerBasePaddingTop, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(binding.playerBarContent, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom + v.getPaddingTop());
            return windowInsets;
        });

        requestNotificationPermission();
        setupViewPager();
        setupPlayerBar();

        LocalBroadcastManager.getInstance(this).registerReceiver(
                playbackReceiver,
                new IntentFilter(MediaService.BROADCAST_PLAYBACK_STATE));
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    private void setupViewPager() {
        StationsPagerAdapter pagerAdapter = new StationsPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(position == 0
                        ? getString(R.string.tab_national)
                        : getString(R.string.tab_local))
        ).attach();
    }

    private void setupPlayerBar() {
        binding.btnPlayPause.setOnClickListener(v -> {
            if (currentStation == null) return;
            if (isPlaying) {
                Intent intent = new Intent(this, MediaService.class);
                intent.setAction(MediaService.ACTION_STOP);
                startService(intent);
            } else {
                playStation(currentStation);
            }
        });
    }

    @Override
    public void onStationSelected(Station station) {
        currentStation = station;
        binding.playerBar.setVisibility(View.VISIBLE);
        binding.playerStationName.setText(station.getName());
        binding.playerNowPlaying.setText("");

        String imageUrl = station.getImageUrl();
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(binding.playerImage);
        } else {
            binding.playerImage.setImageResource(R.drawable.ic_radio);
        }

        playStation(station);
        startEpgPolling();
    }

    private void playStation(Station station) {
        Intent intent = new Intent(this, MediaService.class);
        intent.setAction(MediaService.ACTION_PLAY);
        intent.putExtra(MediaService.EXTRA_STREAM_URL, station.getStreamUrl());
        intent.putExtra(MediaService.EXTRA_STATION_NAME, station.getName());
        ContextCompat.startForegroundService(this, intent);
    }

    private void startEpgPolling() {
        epgHandler.removeCallbacks(epgRunnable);
        fetchNowPlaying();
        epgHandler.postDelayed(epgRunnable, 30000);
    }

    private void fetchNowPlaying() {
        if (currentStation == null || currentStation.getNowPlayingUrl() == null) return;

        ApiClient.getInstance().getNowPlaying(currentStation.getNowPlayingUrl(), new ApiCallback<EpgResponse>() {
            @Override
            public void onSuccess(EpgResponse response) {
                if (response != null && response.tracks != null && !response.tracks.isEmpty()) {
                    EpgTrack track = findCurrentTrack(response);
                    if (track != null) {
                        updateNowPlaying(track);
                    }
                }
            }

            @Override
            public void onFailure(Exception error) {
            }
        });
    }

    private EpgTrack findCurrentTrack(EpgResponse epgResponse) {
        if (epgResponse.tracks == null || epgResponse.tracks.isEmpty()) return null;

        long now = System.currentTimeMillis();
        for (EpgTrack track : epgResponse.tracks) {
            if (track.startTime == null || track.duration == null) continue;
            try {
                long start = Long.parseLong(track.startTime);
                long duration = Long.parseLong(track.duration) * 1000;
                if (now >= start && now < start + duration) {
                    return track;
                }
            } catch (NumberFormatException e) {
            }
        }
        return epgResponse.tracks.get(epgResponse.tracks.size() - 1);
    }

    private void updateNowPlaying(EpgTrack track) {
        binding.playerNowPlaying.setText(track.getDisplayName());
        if (track.hasImage()) {
            Glide.with(this).load(track.image.trim()).into(binding.playerImage);
        }
    }

    private void updatePlayerUI(boolean playing, boolean buffering) {
        binding.btnPlayPause.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);
        binding.bufferingIndicator.setVisibility(buffering ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        epgHandler.removeCallbacks(epgRunnable);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playbackReceiver);
        super.onDestroy();
    }
}
