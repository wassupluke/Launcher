# Building µLauncher

## Using the command line

Install JDK 17 and the Android Sdk.
Make sure that `JAVA_HOME` and `ANDROID_HOME` are set correctly.

```
git clone https://github.com/jrpie/Launcher
cd Launcher

./gradlew assembleDefaultRelease
```

This will create an apk file at `app/build/outputs/apk/default/release/app-default-release-unsigned.apk`.

Note that you need to sign it:
```
apksigner sign --ks "$YOUR_KEYSTORE" \
    --ks-key-alias "$YOUR_ALIAS" \
    --ks-pass="pass:$YOUR_PASSWORD" \
    --key-pass="pass:$YOUR_PASSWORD" \
    --alignment-preserved \
    --v1-signing-enabled=true \
    --v2-signing-enabled=true \
    --v3-signing-enabled=true \
    --v4-signing-enabled=true \
    app-default-release-unsigned.apk
```


See [this guide](https://developer.android.com/build/building-cmdline)
for further instructions.


## Using Android Studio
Install [Android Studio](https://developer.android.com/studio), import this project and build it.

See [this guide](https://developer.android.com/studio/run)
for further instructions.
