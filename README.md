# 🎵 Mica Music - Spotify Local Playlist App

A mobile Android application that connects to the Spotify Web API and plays songs from a local list of artists. Users can toggle between Spanish and English playback using a sticky footer with language flags.

## 🚀 Features

- **Multi-language Support**: Toggle between Spanish and English songs using flag buttons
- **Spotify Integration**: Seamless connection to Spotify Web API for music playback
- **Smart UI**: Songs unavailable in the selected language appear in grayscale and are disabled
- **Local Data**: Uses a local JSON file containing artist information and song URIs
- **Responsive Design**: Scrollable list with beautiful card-based artist display
- **Automated Deployment**: CI/CD pipeline builds and deploys APK via GitHub Actions

## 📦 Local Data Structure

The app reads from a local JSON file (`app/src/main/assets/artists.json`) with this format:

```json
[
  {
    "name": "Shakira",
    "imageUrl": "https://i.scdn.co/image/ab6761610000e5ebcc9dc00cad9092fdb3b137a0",
    "spanishSong": "spotify:track:3sNVsP50132BTNlImLx70i",
    "englishSong": "spotify:track:2dLLR6qlu5UJ5gk0dKz0h3"
  }
]
```

## 🛠️ Local Setup Instructions

### Prerequisites

1. **Android Studio** (latest version recommended)
2. **JDK 17** or higher
3. **Android SDK** (API level 24 or higher)
4. **Spotify Developer Account** and app credentials

### Step 1: Clone the Repository

```bash
git clone https://github.com/hector-almagro-maersk/mica-music.git
cd mica-music
```

### Step 2: Get Spotify Credentials

1. Go to [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Create a new app or use an existing one
3. Note down your **Client ID** and **Client Secret**
4. Add `mica-music://callback` as a redirect URI in your app settings

### Step 3: Download Spotify App Remote SDK

1. Visit [Spotify Android SDK Documentation](https://developer.spotify.com/documentation/android/)
2. Download the Spotify App Remote SDK
3. Extract the `spotify-app-remote-release-0.8.0.aar` file
4. Place it in the `app/libs/` directory

### Step 4: Configure Environment

Create a `local.properties` file in the root directory:

```properties
SPOTIFY_CLIENT_ID=your_client_id_here
SPOTIFY_CLIENT_SECRET=your_client_secret_here
```

### Step 5: Update Spotify Client ID

Open `app/src/main/java/com/micamusic/app/service/SpotifyService.kt` and replace:

```kotlin
private const val CLIENT_ID = "your_client_id_here"
```

With your actual Spotify Client ID.

### Step 6: Build and Run

#### Option A: Using Android Studio
1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Connect an Android device or start an emulator
4. Click "Run" or press `Ctrl+R` (Windows/Linux) / `Cmd+R` (Mac)

#### Option B: Using Command Line
```bash
# Build the project
./gradlew build

# Install on connected device
./gradlew installDebug

# Or build APK for manual installation
./gradlew assembleDebug
```

The APK will be generated in `app/build/outputs/apk/debug/`

## 🔧 Development

### Running Tests
```bash
./gradlew test
```

### Building Release APK
```bash
./gradlew assembleRelease
```

### Debugging with Emulator
1. Start Android Virtual Device (AVD) from Android Studio
2. Ensure Spotify app is installed on the emulator
3. Run the app and test Spotify authentication

### Testing with Physical Device
1. Enable Developer Options and USB Debugging on your Android device
2. Install Spotify app from Google Play Store
3. Connect device via USB
4. Run the app from Android Studio

## 🔐 CI/CD and Deployment

The project includes a GitHub Actions workflow that:

1. **Builds the APK** automatically on push to main branch
2. **Uploads to GitHub Releases** when a new release is created
3. **Uses GitHub Secrets** for secure credential management

### Required GitHub Secrets

Set these in your repository settings under Secrets and Variables → Actions:

- `SPOTIFY_CLIENT_ID`: Your Spotify app client ID
- `SPOTIFY_CLIENT_SECRET`: Your Spotify app client secret
- `SIGNING_KEY`: Base64 encoded signing key for release builds (optional)
- `ALIAS`: Key alias for signing (optional)
- `KEY_STORE_PASSWORD`: Keystore password (optional)
- `KEY_PASSWORD`: Key password (optional)

### Creating a Release

1. Create a new tag: `git tag v1.0.0`
2. Push the tag: `git push origin v1.0.0`
3. Create a GitHub Release from the tag
4. The workflow will automatically build and attach the APK

## 🎨 UI/UX Features

- **Sticky Footer**: Language toggle buttons remain visible while scrolling
- **Dynamic Status**: Visual indicators show song availability per language
- **Grayscale Effect**: Unavailable songs are visually distinct
- **Smooth Interactions**: Responsive touch feedback and animations
- **Material Design**: Modern Android UI following Material Design guidelines

## 🔧 Troubleshooting

### Build Issues

If you encounter build failures, please refer to our comprehensive **[Build Troubleshooting Guide](BUILD_TROUBLESHOOTING.md)** which covers:

- Network connectivity issues (firewall blocking `dl.google.com`)
- Android SDK configuration problems
- Memory and Gradle daemon issues
- Environment-specific solutions for GitHub Actions, local development, and corporate networks

### Common Issues

1. **Spotify Authentication Fails**
   - Ensure Spotify app is installed on the device
   - Verify redirect URI is correctly configured
   - Check Client ID in the source code

2. **Build Errors**
   - **Network connectivity**: See [Build Troubleshooting Guide](BUILD_TROUBLESHOOTING.md) for detailed solutions
   - **Local development**: Ensure you have a stable internet connection and check corporate firewall settings
   - **GitHub Actions**: The workflow includes enhanced error handling and diagnostics
   - Ensure all dependencies are downloaded with: `./gradlew --refresh-dependencies clean build`
   - Check that Spotify App Remote AAR is in `app/libs/`
   - Verify Android SDK and build tools are updated

3. **App Crashes on Startup**
   - Check device logs with `adb logcat`
   - Ensure minimum SDK version (API 24) is met
   - Verify permissions in AndroidManifest.xml

### Network Requirements for Building

The Android build system requires internet access to these domains:
- `dl.google.com` - Android SDK components and Gradle Plugin
- `repo1.maven.org` - Maven Central dependencies  
- `services.gradle.org` - Gradle distributions

If any of these are blocked by firewall rules, the build will fail with network connectivity errors.

### Debug Logs

Enable verbose logging by adding to your `local.properties`:
```properties
android.enableVerboseLogging=true
```

## 📱 System Requirements

- **Minimum Android Version**: Android 7.0 (API level 24)
- **Target Android Version**: Android 14 (API level 34)
- **Required Apps**: Spotify app must be installed on the device
- **Internet Connection**: Required for Spotify authentication and streaming

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit your changes: `git commit -am 'Add new feature'`
4. Push to the branch: `git push origin feature/new-feature`
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Spotify for their comprehensive Web API and Android SDK
- Material Design team for UI/UX guidelines
- Android development community for best practices and tools