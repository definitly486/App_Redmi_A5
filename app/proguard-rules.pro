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

# BouncyCastle — обязательно для всех версий
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Если используешь рефлексию или динамическую загрузку провайдеров
-keep class org.bouncycastle.jce.provider.BouncyCastleProvider { *; }
-keep class org.bouncycastle.jsse.provider.BouncyCastleJsseProvider { *; }

# Дополнительно на всякий случай (для OpenPGP, X.509 и т.д.)
-keep class org.bouncycastle.openpgp.** { *; }
-keep class org.bouncycastle.crypto.** { *; }

# ───────────────────────────────
# Bouncy Castle — полная защита от R8
# ───────────────────────────────
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-keep class org.bouncycastle.jcajce.** { *; }
-keep class org.bouncycastle.crypto.** { *; }
-keep class org.bouncycastle.openpgp.** { *; }
-keep class org.bouncycastle.x509.** { *; }

# Если используешь Provider (почти всегда да)
-keep class org.bouncycastle.jce.provider.BouncyCastleProvider { *; }
-keep class org.bouncycastle.jsse.provider.BouncyCastleJsseProvider { *; }

# На всякий случай — если где-то используешь ServiceLoader
-keep class ** extends java.security.Provider { *; }
# Не падать, если классы отсутствуют
-dontwarn javax.management.**
-dontwarn java.lang.ProcessHandle
-dontwarn org.ietf.jgss.**
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Не обфусцировать JGit
-keep class org.eclipse.jgit.** { *; }
-keep class org.slf4j.** { *; }

-dontwarn java.lang.ProcessHandle
-dontwarn javax.management.**
-dontwarn org.ietf.jgss.**
-dontwarn java.lang.management.**
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.**

# JGit core
-keep class org.eclipse.jgit.** { *; }
-dontwarn org.eclipse.jgit.**
