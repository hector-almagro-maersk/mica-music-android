# Build Troubleshooting Guide

This document provides solutions for common build issues encountered when building the Mica Music Android app.

## Common Build Failures

### 1. Network Connectivity Issues

**Error:** `Could not resolve com.android.tools.build:gradle:X.X.X`
**Cause:** Firewall blocking access to Google's Maven repository (`dl.google.com`)

**Solutions:**
- **For GitHub Actions:** The workflow includes retry logic and network diagnostics
- **For Local Development:** Check your internet connection and corporate firewall settings
- **For CI/CD:** Ensure the build environment has access to:
  - `dl.google.com` (Google's Maven repository)
  - `repo1.maven.org` (Maven Central)
  - `services.gradle.org` (Gradle distributions)

### 2. Android SDK Issues

**Error:** `Failed to find target with hash string 'android-34'`
**Cause:** Missing Android SDK components

**Solutions:**
1. Install Android SDK Platform 34:
   ```bash
   sdkmanager "platforms;android-34"
   ```
2. Install required build tools:
   ```bash
   sdkmanager "build-tools;34.0.0"
   ```

### 3. Memory Issues

**Error:** `OutOfMemoryError` during build
**Cause:** Insufficient heap memory for Gradle

**Solutions:**
1. Increase Gradle heap size in `gradle.properties`:
   ```properties
   org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
   ```
2. For GitHub Actions, the workflow is configured with appropriate memory settings

### 4. Gradle Daemon Issues

**Error:** Various intermittent build failures
**Cause:** Corrupted Gradle daemon

**Solutions:**
1. Stop all Gradle daemons:
   ```bash
   ./gradlew --stop
   ```
2. Clean and rebuild:
   ```bash
   ./gradlew clean build
   ```

## Environment-Specific Solutions

### GitHub Actions

The GitHub Actions workflow includes:
- ✅ Network connectivity checks
- ✅ Retry logic for dependency downloads
- ✅ Enhanced error diagnostics
- ✅ Proper Android SDK setup
- ✅ Gradle cache optimization

### Local Development

1. **Android Studio Setup:**
   - Install Android Studio Arctic Fox or newer
   - Install Android SDK 34
   - Install Android Build Tools 34.0.0

2. **Command Line Setup:**
   ```bash
   # Set ANDROID_HOME environment variable
   export ANDROID_HOME=/path/to/android/sdk
   export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
   ```

3. **Gradle Wrapper:**
   Always use the project's Gradle wrapper:
   ```bash
   ./gradlew build
   ```

### Corporate Networks

If building behind a corporate firewall:

1. **Configure Gradle proxy settings:**
   Add to `gradle.properties`:
   ```properties
   systemProp.http.proxyHost=your.proxy.host
   systemProp.http.proxyPort=8080
   systemProp.https.proxyHost=your.proxy.host
   systemProp.https.proxyPort=8080
   ```

2. **Request firewall exceptions for:**
   - `dl.google.com`
   - `repo1.maven.org`
   - `services.gradle.org`
   - `jcenter.bintray.com`

## Build Optimization Tips

1. **Enable parallel builds:**
   ```properties
   org.gradle.parallel=true
   ```

2. **Use build cache:**
   ```properties
   org.gradle.caching=true
   ```

3. **Configure memory appropriately:**
   ```properties
   org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
   ```

## Diagnostic Commands

Run these commands to diagnose build issues:

```bash
# Check Gradle version
./gradlew --version

# List dependencies
./gradlew dependencies

# Run build with debug info
./gradlew build --info --stacktrace

# Check Android SDK
echo $ANDROID_HOME
ls -la $ANDROID_HOME/platforms/

# Test network connectivity
curl -I https://dl.google.com/dl/android/maven2/
```

## Getting Help

If you encounter build issues not covered here:

1. Check the GitHub Actions build logs for detailed error messages
2. Run the diagnostic commands listed above
3. Include the full error log when reporting issues
4. Specify your environment (OS, Java version, Android Studio version)

## Continuous Integration

The project uses GitHub Actions for automated builds. The workflow:
- Runs on Ubuntu latest
- Uses JDK 17 (Temurin distribution)
- Includes comprehensive error handling and diagnostics
- Caches dependencies for faster builds
- Automatically retries failed dependency downloads

For successful CI builds, ensure your repository has access to the required external services listed in the network connectivity section.