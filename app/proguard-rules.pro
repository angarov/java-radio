# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# SimpleXML
-keep class org.simpleframework.** { *; }
-keep class il.co.radio.model.** { *; }
-keepclassmembers class il.co.radio.model.** { *; }

# Gson
-keep class com.google.gson.** { *; }
