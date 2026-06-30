# ═══════════════════════════════════════════════
# Mana Delivery Bellempally — ProGuard Rules
# ═══════════════════════════════════════════════

# ── Firebase ──
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ── App Data Models (Firestore deserialization) ──
-keep class com.example.manadeliverybellempally.data.model.** { *; }
-keepclassmembers class com.example.manadeliverybellempally.data.model.** {
    *;
}

# ── Kotlin Serialization ──
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}
-keep,includedescriptorclasses class com.example.manadeliverybellempally.**$$serializer { *; }
-keepclassmembers class com.example.manadeliverybellempally.** {
    *** Companion;
}

# ── Razorpay Payment Gateway ──
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
-keepattributes *Annotation*
-dontwarn com.razorpay.**
-keep class com.razorpay.** { *; }
-optimizations !method/inlining/*
-keepclasseswithmembers class * {
    public void onPayment*(...);
}

# ── Google Maps & Play Services ──
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.**

# ── Coil Image Loading ──
-dontwarn coil3.**
-keep class coil3.** { *; }

# ── Google Generative AI ──
-dontwarn com.google.ai.client.generativeai.**
-keep class com.google.ai.client.generativeai.** { *; }

# ── OkHttp (used by Coil) ──
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# ── General Android ──
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
