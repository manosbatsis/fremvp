# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/Omjoon/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

#noinspection ShrinkerUnresolvedReference

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

### RxJava, RxAndroid (https://gist.github.com/kosiara/487868792fbd3214f9c9)
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}

-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}

-keep class rx.schedulers.TestScheduler {
    public <methods>;
}

-keep class rx.schedulers.Schedulers {
    public static ** test();
}

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
#noinspection ShrinkerUnresolvedReference
    long producerNode;
    long consumerNode;
}

-dontwarn sun.misc.Unsafe

-dontwarn org.reactivestreams.FlowAdapters
-dontwarn org.reactivestreams.**
-dontwarn java.util.concurrent.flow.**
-dontwarn java.util.concurrent.**


### LeakCanary
-keep class org.eclipse.mat.** { *; }
-keep class com.squareup.leakcanary.** { *; }

### Support v7, Design
# http://stackoverflow.com/questions/29679177/cardview-shadow-not-appearing-in-lollipop-after-obfuscate-with-proguard/29698051
#-keep class android.support.v7.widget.RoundRectDrawable { *; }
#
#-keep public class android.support.v7.widget.** { *; }
#-keep public class android.support.v7.internal.widget.** { *; }
#-keep public class android.support.v7.internal.view.menu.** { *; }
#
#-keep public class * extends android.support.v4.view.ActionProvider {
#    public <init>(android.content.Context);
#}

-dontwarn android.support.**
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

# https://github.com/Gericop/Android-Support-Preference-V7-Fix/blob/master/preference-v7/proguard-rules.pro
#-keepclassmembers class android.support.v7.preference.PreferenceGroupAdapter {
#    private ** mPreferenceLayouts;
#}
#-keepclassmembers class android.support.v7.preference.PreferenceGroupAdapter$PreferenceLayout {
#    private int resId;
#    private int widgetResId;
#}

### Retrolambda
# as per official recommendation: https://github.com/evant/gradle-retrolambda#proguard
-dontwarn java.lang.invoke.*

# If you do not use RxJava:
-dontwarn rx.**

### Other
-dontwarn com.google.errorprone.annotations.*

### Android Architecture Components
# Ref: https://issuetracker.google.com/issues/62113696
# LifecycleObserver's empty constructor is considered to be unused by proguard
-keep class * implements android.arch.lifecycle.LifecycleObserver {
    <init>(...);
}

# ViewModel's empty constructor is considered to be unused by proguard
-keepclassmembers class * extends android.arch.lifecycle.ViewModel {
    <init>(...);
}

-keepnames class com.cyberlogitec.freight9.common.RootViewModel
-keepclassmembers class * extends com.cyberlogitec.freight9.common.RootViewModel { <init>(...); }

# keep Lifecycle State and Event enums values
-keepclassmembers class android.arch.lifecycle.Lifecycle$State { *; }
-keepclassmembers class android.arch.lifecycle.Lifecycle$Event { *; }

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# https://github.com/Kotlin/kotlinx.atomicfu/issues/57
-dontwarn kotlinx.atomicfu.**

### Kotlin
#https://stackoverflow.com/questions/33547643/how-to-use-kotlin-with-proguard
#https://medium.com/@AthorNZ/kotlin-metadata-jackson-and-proguard-f64f51e5ed32
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

### Adjust SDK, android.installreferrer
-keep public class com.adjust.sdk.** { *; }
-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep public class com.android.installreferrer.** { *; }

### Android Iconics
#-keep class .R
-keep class **.R$* {
    <fields>;
}

### Nothing for Butterknife 8, Realm, RxJava2, RxBinding, RxRelay, Dagger2, OneSignal, Google Play Services, Firebase, Facebook SDK, Room DB,
