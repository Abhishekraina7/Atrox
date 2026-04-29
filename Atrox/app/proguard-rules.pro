# Preserve Google Play Services Auth/Identity classes for unmarshalling
-keep class com.google.android.gms.auth.api.identity.** { *; }
-keep class com.google.android.gms.common.internal.safeparcel.SafeParcelable { *; }

# If you use Credential Manager
-keep class androidx.credentials.** { *; }

# Standard ProGuard rules for Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}