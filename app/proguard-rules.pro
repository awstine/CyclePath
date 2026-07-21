# Attributes to keep - Signature is critical for Retrofit/Gson generic types
-keepattributes Signature, InnerClasses, EnclosingMethod, AnnotationDefault, *Annotation*

# Retrofit 2 rules
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**

# Keep Retrofit service interfaces and their methods
-keep interface * {
    @retrofit2.http.* <methods>;
}

# Critical fix for Retrofit + Coroutines (suspend functions)
# Preserves the generic signature of the Continuation parameter
-keepnames class kotlin.coroutines.Continuation

# Gson rules
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.TypeAdapter
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep our API and Data models (critical for JSON parsing)
# We keep both the class and its members to ensure field names match JSON keys
-keep class com.siaka.data.** { *; }
-keepclassmembers class com.siaka.data.** { *; }

# Keep BuildConfig to ensure API keys are accessible
-keep class com.siaka.BuildConfig { *; }

# Hilt rules
-keep class com.siaka.**_HiltComponents* { *; }
-keep class dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }

# Mapbox specific
-keep class com.mapbox.** { *; }
-dontwarn com.mapbox.**

# OkHttp rules
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# Keep enum members (Gson often uses them)
-keepclassmembers enum * { *; }
