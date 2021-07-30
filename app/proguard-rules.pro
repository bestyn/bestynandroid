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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#---------------------------- Bestyn BEGIN ----------------------------
-keep class com.gbksoft.neighbourhood.data.models.** { *; }
-keep class com.gbksoft.neighbourhood.data.shared_prefs.CurrentProfileModel
#---------------------------- Bestyn END ----------------------------

#---------------------------- Crashlytics BEGIN ----------------------------
-keepattributes *Annotation*                      # Keep Crashlytics annotations
-keepattributes SourceFile,LineNumberTable        # Keep file names/line numbers
-keep public class * extends java.lang.Exception  # Keep custom exceptions (opt)
#---------------------------- Crashlytics END ----------------------------

#---------------------------- Glige BEGIN ----------------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
#---------------------------- Glige END ----------------------------
-keepclassmembers public class com.gbksoft.neighbourhood.ui.data_binding.** { *; }
-keep class android.databinding.** { *; }
-keepattributes *Annotation*
-keepattributes javax.xml.bind.annotation.*
-keepattributes javax.annotation.processing.*

-keepclassmembers class ** {
    @android.databinding.BindingAdapter public *;
}
-dontwarn android.databinding.**