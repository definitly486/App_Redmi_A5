package com.example.app

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.app.fragments.RootChecker
import java.io.File

object ApkAutoInstaller {

    private val LOG_TAG = "InstallAutoAPK"

    fun installAutoAPK(context: Context?) {




        if (context == null) {
            Log.e(LOG_TAG, "Context is null → автоустановка отменена")
            return
        }

        if (!RootChecker.hasRootAccess(context)) {
            Toast.makeText(
                context,
                "Root-доступ отсутствует. Aвтоустановка отменена.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (context == null) {
            Log.e(LOG_TAG, "Context is null → автоустановка отменена")
            return
        }

        val packageManager = context.packageManager

        // Список APK-файлов (можно менять как угодно)
        val apks = listOf(
            "Total_Commander_v.3.50d.apk",
            "k9mail-13.0.apk",
            "Google+Authenticator+7.0.apk",
            "Pluma_.private_fast.browser_1.80_APKPure.apk",
            "com.aurora.store_70.apk",
            "ByeByeDPI-arm64-v8a-release.apk",
            "Telegram+X+0.27.5.1747-arm64-v8a.apk",
            "Core+Music+Player_1.0.apk"
        )

        // Путь к папке с APK (оставил твой оригинальный, но с безопасным созданием)
        val appApkDir = File(
            Environment.getExternalStorageDirectory(),
            "/Android/data/${context.packageName}/files/APK"
        ).also { dir ->
            if (!dir.exists()) {
                val created = dir.mkdirs()
                Log.i(LOG_TAG, if (created) "Директория создана: ${dir.absolutePath}"
                else "Не удалось создать директорию: ${dir.absolutePath}")
            }
        }

        Log.i(LOG_TAG, "Запуск автоустановки APK из: ${appApkDir.absolutePath}")

        for (apkFileName in apks) {
            val apkFile = File(appApkDir, apkFileName)

            if (!apkFile.exists()) {
                Log.w(LOG_TAG, "Файл не найден → пропуск: $apkFileName")
                continue
            }

            // Автоматически получаем package name из APK
            val packageName = getPackageNameFromApk(context, apkFile)
            if (packageName == null) {
                Log.e(LOG_TAG, "Не удалось прочитать package name из APK → пропуск: $apkFileName")
                continue
            }

            // Проверяем, установлен ли уже пакет
            val alreadyInstalled = isPackageInstalled(packageManager, packageName)

            if (alreadyInstalled) {
                Log.i(LOG_TAG, "Уже установлен → пропуск: $apkFileName [$packageName]")
                continue
            }

            Log.i(LOG_TAG, "Установка: $apkFileName → $packageName")

            try {
                // Временно отключаем SELinux (если нужно на твоём устройстве)
                Runtime.getRuntime().exec(arrayOf("su", "-c", "setenforce 0"))

                // pm install -r = переустановка, если вдруг старая версия есть (на всякий случай)
                val cmd = "pm install -r \"${apkFile.absolutePath}\""
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))

                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    Log.i(LOG_TAG, "Успешно установлен: $apkFileName → $packageName")
                } else {
                    val error = process.errorStream.bufferedReader().use { it.readText() }.trim()
                    Log.e(LOG_TAG, "Ошибка установки $apkFileName (код $exitCode): $error")
                }

                // Возвращаем SELinux в безопасное состояние
                Runtime.getRuntime().exec(arrayOf("su", "-c", "setenforce 1")).waitFor()

            } catch (e: Exception) {
                Log.e(LOG_TAG, "Исключение при установке $apkFileName", e)
            }

            // Задержка между установками (чтобы система не захлебнулась)
            Thread.sleep(700)
        }

        Log.i(LOG_TAG, "Автоустановка APK завершена.")
    }

    // Вспомогательная функция: получить package name из APK
    private fun getPackageNameFromApk(context: Context, apkFile: File): String? {
        return try {
            val pm = context.packageManager
            val archiveInfo = pm.getPackageArchiveInfo(apkFile.absolutePath, 0)
            archiveInfo?.packageName?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Ошибка чтения package name из ${apkFile.name}", e)
            null
        }
    }

    // Вспомогательная функция: проверка установки пакета
    private fun isPackageInstalled(pm: PackageManager, packageName: String): Boolean {
        return try {
            pm.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Ошибка проверки установки пакета $packageName", e)
            false
        }
    }

}