# Add project specific ProGuard rules here.
-keep class com.peopleapp.data.** { *; }
-keep class com.peopleapp.data.model.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
