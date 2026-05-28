# Keep Compose
-keep class androidx.compose.** { *; }

# Keep Room entities & DAOs
-keep class com.example.dumbbellworkout.data.db.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Gson (пока используется)
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.example.dumbbellworkout.** { *; }

# Kotlinx Serialization (для этапа 3)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.example.dumbbellworkout.**$$serializer { *; }
-keepclassmembers class com.example.dumbbellworkout.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.dumbbellworkout.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Coroutines
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
