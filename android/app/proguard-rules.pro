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


-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn com.google.common.**

# ------------ SDK KEEPING -----------------------------------
-keep public class vn.jth.xverifysdk.data.* { *; }
-keep public class vn.jth.xverifysdk.ekyc.* { *; }
-keep public class vn.jth.xverifysdk.ekyc.core.* { *; }
-keep public class vn.jth.xverifysdk.card.* { *; }
-keep public class vn.jth.xverifysdk.ekyc.core.StepFace { *; }
-keep public class vn.jth.xverifysdk.jmrtd.VerificationStatus { *; }
-keep public class vn.jth.xverifysdk.jmrtd.VerificationStatus$* { *; }
-keep public class vn.jth.xverifysdk.jmrtd.FeatureStatus { *; }
-keep public class vn.jth.xverifysdk.jmrtd.FeatureStatus$* { *; }
-keep public class vn.jth.xverifysdk.utils.StringUtils { *; }
-keep public class vn.jth.xverifysdk.utils.HashedUtils { *; }
                # ------ Ekyc -----#
-keepclasseswithmembers class vn.jth.xverifysdk.ekyc.core.EkycUtils {
    public static android.graphics.Bitmap fixOrientation(...);
    public static android.graphics.Bitmap scaleDown(...);
    public static storeBitmapToFile(...);
    public static cropBitmapWithAspectRatio(...);
}
-keep public class vn.jth.xverifysdk.ekyc.core.StepFace { *; }
                # ------ CECA -----#
-keepclasseswithmembers class vn.jth.xverifysdk.utils.CecaUtils {
    public static boolean verifySignature (...);
    public static generateSignature(...);
    public static generateMessageId(...);
}
# ------------------------------------------------------------



# ---------- Important ---------------------------------------
-keep class org.jmrtd.** { *; }
-keep class org.jmrtd.lds.icao.** { *; }
-keep class org.jmrtd.lds.** { *; }
-keep class org.spongycastle.jce.** { *; }
-keep class org.spongycastle.jcajce.provider.** { *; }
-keep class org.spongycastle.jcajce.provider.drbg.** { *; }
-keep class org.spongycastle.jcajce.provider.keystore.** { *; }
-keep class org.spongycastle.jcajce.provider.digest.** { *; }
-keep class org.spongycastle.jcajce.provider.asymmetric.** { *; }
-keep class org.spongycastle.jcajce.provider.symmetric.** { *; }
-keep class org.bouncycastle.jcajce.provider.digest.** { *; }
-keep class org.bouncycastle.jcajce.provider.symmetric.** { *; }
-keep class org.bouncycastle.jcajce.provider.keystore.** { *; }
-keep class org.bouncycastle.jcajce.provider.drbg.** { *; }
-keep class org.bouncycastle.jcajce.provider.asymmetric.** { *; }
# -------------------------------------------------------------



# ---------- Networks -----------------------------------------
-keep public class vn.jth.xverifysdk.network.* { *; }
-keep public class vn.jth.xverifysdk.network.models.* { *; }
-keep public class vn.jth.xverifysdk.network.models.request.* { *; }
-keep public class vn.jth.xverifysdk.network.models.response.* { *; }
-keep public class vn.jth.xverifysdk.network.models.response.face.* { *; }
-keep public class vn.jth.xverifysdk.network.models.response.bio.* { *; }
-keep public class vn.jth.xverifysdk.network.models.request.bio.* { *; }
-keepattributes Exceptions, Signature, InnerClasses
# --------------------------------------------------------------



# ---------- Camera --------------------------------------------
-keep public class io.fotoapparat.view.CameraView { *; }
-keep public class io.fotoapparat.** { *; }
# --------------------------------------------------------------



# ----------- Open source library ------------------------------
-keep public class net.sf.scuba.** { *; }
-keep public class retrofit2.** { *; }
-keep public class io.** { *; }
# --------------------------------------------------------------

-keep class vn.jth.xverifysdk.onboard.* { *; }
-keep public class vn.jth.xverifysdk.pack.* { *; }
-keep public class vn.jth.xverifysdk.pack.selector.* { *; }
-keepattributes Exceptions, Signature, InnerClasses
