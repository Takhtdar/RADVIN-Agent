#!/bin/bash

set -e

# Clean build folder
rm -rf build/*
mkdir -p build




# Package resources and generate R.java
~/Android/Sdk/build-tools/24.0.0/aapt package -f -m \
    -J build \
    -S helloworld/res \
    -I ~/Android/Sdk/platforms/android-19/android.jar \
    -M helloworld/AndroidManifest.xml \
    -F build/app.unsigned.apk


# Compile Java
javac -source 1.7 -target 1.7 \
    -bootclasspath ~/Android/Sdk/platforms/android-19/android.jar \
    -d build \
    helloworld/src/com/example/helloworld/HelloActivity.java \
    helloworld/src/com/example/helloworld/ClipboardLoggerService.java \
    helloworld/src/com/example/helloworld/BootReceiver.java \
    helloworld/src/com/example/helloworld/ReturnHello.java \
    helloworld/src/com/example/helloworld/ClipboardDatabaseHelper.java \
    build/com/example/helloworld/R.java 





# Convert to .dex
~/Android/Sdk/build-tools/24.0.0/dx --dex --output=build/classes.dex build/

# Add .dex to APK
cd build
#zip -r app.unsigned.apk classes.dex
zip -u app.unsigned.apk classes.dex


# keytool -genkey -v -keystore ~/.android/debug2.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -validity 100000 -dname "CN=Android Debug,O=Android,C=US"


# algorithm for signing is very important

# Sign it
jarsigner -verbose -keystore ~/.android/debug.keystore \
	-storepass android -keypass android \
	-sigalg SHA256withRSA \
	-digestalg SHA-256 \
	app.unsigned.apk androiddebugkey




# Align it
~/Android/Sdk/build-tools/24.0.0/zipalign -v 4 app.unsigned.apk app.signed.apk

# Install it
adb -s emulator-5554 install -r app.unsigned.apk

echo "✅ Done! App installed!"

# adb -s emulator-5554 logcat -s RADVIN
