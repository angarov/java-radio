# Radio

Android radio streaming app for Israeli stations on the 100FM network.

## Features

- Stream national and local radio stations
- Browse stations organized in tabs
- Now-playing information with real-time EPG updates
- Background playback with media notification controls
- Hebrew and English UI support

## Tech Stack

- Java 17
- Android SDK (minSdk 26, targetSdk 35)
- Media3 ExoPlayer with HLS streaming
- Material Design Components
- ViewPager2 with TabLayout

## Build

```
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## License

MIT
