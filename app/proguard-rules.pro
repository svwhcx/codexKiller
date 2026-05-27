-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*

# These build-tool libraries run inside the app during no-env repackaging.
# R8 obfuscation can break their internal lookups and make release builds fail
# only on device, for example during dex rewriting or APK signing.
-keep class com.android.apksig.** { *; }
-keep class org.jf.dexlib2.** { *; }
-keep class lanchon.multidexlib2.** { *; }
-keep class com.google.common.io.ByteStreamsHack { *; }
