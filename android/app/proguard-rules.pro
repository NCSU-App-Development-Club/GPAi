# ---------------------------------------------------------------------------
# GPAi R8 / ProGuard rules
# ---------------------------------------------------------------------------

# --- Retrofit ---------------------------------------------------------------
# Required for R8 (full mode): preserve generic signatures and suspend-function
# continuation metadata. See https://github.com/square/retrofit#proguard
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRE
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, generic signatures are stripped for classes that are not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# --- Gson -------------------------------------------------------------------
# API models are deserialized via reflection on field names (no @SerializedName),
# so the classes themselves must be kept (not just members) to prevent R8 full-mode
# class merging/flattening which causes ClassCastException at runtime.
-keep class org.appdevncsu.gpai.api.models.** {
    <fields>;
    <init>(...);
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type

# --- PDFBox-Android ----------------------------------------------------------
# Optional JPEG2000 decoder dependency referenced but not bundled.
-dontwarn com.gemalto.jp2.**
