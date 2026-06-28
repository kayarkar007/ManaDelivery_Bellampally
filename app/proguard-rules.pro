# Firebase ProGuard rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }

# Kotlin Serialization
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}

# App models
-keep class com.example.manadeliverybellempally.data.model.** { *; }
