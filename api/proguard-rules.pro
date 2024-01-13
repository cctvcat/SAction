-dontwarn android.**
-dontwarn com.android.**

-keepclassmembers class rikka.hidden.compat.adapter.ProcessObserverAdapter {
    <methods>;
}

-keepclassmembers class rikka.hidden.compat.adapter.UidObserverAdapter {
    <methods>;
}

-keepclassmembers class * implements cctvcat.saction.remote.ISaEntry {
    <methods>;
}

-keep class cctvcat.saction.remote.SaService {
    public static void main(java.lang.String[]);
}