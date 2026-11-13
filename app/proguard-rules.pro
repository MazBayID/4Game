# NEO-NULL: PYTHON LEARNING PROTOCOL ProGuard / R8 Rules
# Keep Jetpack Compose & ViewModel classes intact for release builds.

-keep class com.neonull.protocol.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
