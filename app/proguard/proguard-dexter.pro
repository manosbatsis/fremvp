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
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Begin dexter
-renamesourcefileattribute SourceFile

# Preserve all Dexter classes and method names
-keepattributes InnerClasses, Signature, *Annotation*

-keep class com.karumi.dexter.** { *; }
-keep interface com.karumi.dexter.** { *; }
-keepclasseswithmembernames class com.karumi.dexter.** { *; }
-keepclasseswithmembernames interface com.karumi.dexter.** { *; }
# End dexter