# Ktor ve bağımlılıkları (OkHttp, Okio) için genel kurallar
-dontwarn okio.**
-dontwarn org.conscrypt.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }

# SLF4J (Ktor'un loglama sistemi) için kural. Hatanın asıl çözümü budur.
# Bu kural, R8'e bu sınıfı silmemesini, sadece uyarı vermemesini söyler.
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Kotlin Coroutines için kurallar
-keep class kotlinx.coroutines.internal.** { *; }
-keep class kotlin.coroutines.jvm.internal.* {
    <fields>;
    <init>(...);
}

# Kotlinx Serialization için kurallar
-keep class kotlinx.serialization.** { *; }
-keep class **$$serializer { *; }
-keepnames class * implements kotlinx.serialization.KSerializer