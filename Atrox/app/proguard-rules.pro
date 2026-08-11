# Preserve Google Play Services Auth/Identity classes for unmarshalling
-keep class com.google.android.gms.auth.api.identity.** { *; }
-keep class com.google.android.gms.common.internal.safeparcel.SafeParcelable { *; }

# If you use Credential Manager
-keep class androidx.credentials.** { *; }

# Standard ProGuard rules for Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Domain Models and Data Transfer Objects (DTOs) for Serialization / Firestore
-keep class com.example.atrox.domain.model.** { *; }
-keep class com.example.atrox.data.remote.dto.** { *; }

# Firebase & Firestore Models
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn javax.annotation.**

# stack traces from Play Console crash reports to be usable
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile