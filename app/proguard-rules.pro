# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Moshi
-keep class * extends com.squareup.moshi.JsonAdapter { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }

# Hilt & Dagger
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.lifecycle.HiltViewModel class *

# Room
-keep class * extends androidx.room.RoomDatabase { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

