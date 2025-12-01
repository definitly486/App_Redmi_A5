@file:Suppress("SpellCheckingInspection")



import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.example.app.fragments.RootChecker
import java.io.File

class DownloadHelper(private val context: Context) {

    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private var currentDownloadId: Long = -1L
    private var downloadReceiver: BroadcastReceiver? = null

    // region === Папки ===

    private fun appDownloadsDir(): File? = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        ?.also { it.mkdirs() }

    private fun appApkDir(): File? = context.getExternalFilesDir("APK")
        ?.also { it.mkdirs() }

    private fun publicDownloadsDir(): File? = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS
    )?.also { it.mkdirs() }

    // Для совместимости с твоим старым кодом
    fun getDownloadFolderapk(): File? = appApkDir()

    // endregion

    // region === Публичные методы загрузки ===

    /** Загрузка APK в стандартную папку приложения */
    fun downloadApk(url: String, onComplete: ((File?) -> Unit)? = null) {
        downloadFile(
            url = url,
            destinationDir = appDownloadsDir(),
            expectedExtension = "apk",
            onExists = { file, name -> installApk(file, name) },
            onSuccess = { file, name -> installApk(file, name) },
            onComplete = onComplete
        )
    }

    /** Загрузка APK в специальную папку /Android/data/.../files/APK/ */
    fun downloadApkToApkFolder(url: String, onComplete: ((File?) -> Unit)? = null) {
        downloadFile(
            url = url,
            destinationDir = appApkDir(),
            subDirName = "APK",
            expectedExtension = "apk",
            onExists = { file, name -> installApk(file, name, useApkFolder = true) },
            onSuccess = { file, name -> installApk(file, name, useApkFolder = true) },
            onComplete = onComplete
        )
    }

    /** Произвольный файл (без установки) */
    fun downloadFileSimple(url: String, onComplete: ((File?) -> Unit)? = null) {
        downloadFile(
            url = url,
            destinationDir = appDownloadsDir(),
            onComplete = onComplete
        )
    }

    /** Загрузка инструмента (busybox, openssl, gh и т.д.) */
    fun downloadTool(url: String, toolName: String, onComplete: ((File?) -> Unit)? = null) {
        downloadFile(
            url = url,
            destinationDir = appDownloadsDir(),
            onExists = { _, _ -> installTool(toolName) },
            onSuccess = { _, _ -> installTool(toolName) },
            onComplete = onComplete
        )
    }

    /** Простая загрузка в публичную папку Downloads (без отслеживания) */
    fun downloadToPublic(url: String) {
        val fileName = url.substringAfterLast("/").substringBefore("?").substringBefore("#")
        val dir = publicDownloadsDir() ?: run {
            toast("Нет доступа к папке загрузок")
            return
        }

        if (File(dir, fileName).exists()) {
            toast("Файл уже существует")
            return
        }

        enqueue(
            url = url,
            title = fileName,
            destination = DownloadDestination.Public(dir, fileName)
        )
        toast("Загрузка начата")
    }

    // endregion

    // region === Универсальная загрузка ===

    private fun downloadFile(
        url: String,
        destinationDir: File?,
        subDirName: String = Environment.DIRECTORY_DOWNLOADS,
        expectedExtension: String? = null,
        onExists: ((File, String) -> Unit)? = null,
        onSuccess: ((File, String) -> Unit)? = null,
        onComplete: ((File?) -> Unit)? = null
    ) {
        if (destinationDir == null) {
            toast("Не удалось получить папку для загрузки")
            onComplete?.invoke(null)
            return
        }

        val fileName = url.substringAfterLast("/")
            .substringBefore("?")
            .substringBefore("#")

        // При желании можно вернуть проверку расширения
        // if (expectedExtension != null && !fileName.endsWith(".$expectedExtension", ignoreCase = true)) { ... }

        val targetFile = File(destinationDir, fileName)
        if (targetFile.exists()) {
            toast("Файл уже существует")
            onExists?.invoke(targetFile, fileName)
            onComplete?.invoke(targetFile)
            return
        }

        registerReceiverIfNeeded { downloadedFile ->
            if (downloadedFile != null) onSuccess?.invoke(downloadedFile, fileName)
            onComplete?.invoke(downloadedFile)
        }

        enqueue(
            url = url,
            title = fileName,
            destination = DownloadDestination.Private(context, subDirName, fileName)
        )
        toast("Загрузка начата: $fileName")
    }

    private sealed class DownloadDestination {
        class Public(val dir: File, val fileName: String) : DownloadDestination()
        class Private(val context: Context, val subDir: String, val fileName: String) : DownloadDestination()
    }

    private fun enqueue(url: String, title: String, destination: DownloadDestination) {
        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setTitle(title)
                setDescription("Загружается...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                allowScanningByMediaScanner()

                when (destination) {
                    is DownloadDestination.Public -> setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        destination.fileName
                    )
                    is DownloadDestination.Private -> setDestinationInExternalFilesDir(
                        destination.context,
                        destination.subDir,
                        destination.fileName
                    )
                }
            }
            currentDownloadId = downloadManager.enqueue(request)
            Log.d("DownloadHelper", "Started download: $title (id=$currentDownloadId)")
        } catch (e: Exception) {
            e.printStackTrace()
            toast("Ошибка при старте загрузки: ${e.message}")
        }
    }

    // endregion

    // region === BroadcastReceiver ===

    private fun registerReceiverIfNeeded(onFinished: (File?) -> Unit) {
        if (downloadReceiver != null) return

        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return
                if (id != currentDownloadId) return

                val file = getDownloadedFile(id)
                Handler(Looper.getMainLooper()).post {
                    toast(if (file != null) "Загрузка завершена" else "Ошибка загрузки")
                    onFinished(file)
                }
                unregisterReceiver()
            }
        }

        context.registerReceiver(
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            Context.RECEIVER_EXPORTED
        )
    }

    private fun getDownloadedFile(downloadId: Long): File? {
        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query)?.use { cursor ->
            if (cursor.moveToFirst()) {
                if (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) ==
                    DownloadManager.STATUS_SUCCESSFUL
                ) {
                    val uriStr = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                    val uri = Uri.parse(uriStr) ?: return null
                    val file = File(uri.path ?: return null)
                    if (file.exists()) return file
                }
            }
        }
        return null
    }

    private fun unregisterReceiver() {
        downloadReceiver?.let {
            try { context.unregisterReceiver(it) } catch (_: Exception) {}
        }
        downloadReceiver = null
        currentDownloadId = -1L
    }

    // endregion

    // region === Установка APK ===

    private fun installApk(file: File, fileName: String? = null, useApkFolder: Boolean = false) {
        if (file.exists().not()) {
            toast("APK файл не найден")
            return
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
                toast("Разрешите установку из неизвестных источников")
                return
            }
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            toast("Не удалось запустить установщик")
        }
    }

    // Установка из стандартной папки приложения
    fun installGate(filename: String) {
        val file = File(appDownloadsDir() ?: return, filename)
        installApk(file)
    }

    // Установка из папки APK
    fun installApkFromApkFolder(filename: String) {
        val file = File(appApkDir() ?: return, filename)
        installApk(file, useApkFolder = true)
    }

    // endregion

    // region === Установка инструментов (root) ===

    private fun installTool(toolName: String) {
        if (!RootChecker.hasRootAccess(context)) {
            alert("Root-доступ отсутствует")
            return
        }
        if (!RootChecker.checkWriteAccess("/system")) {
            alert("Нет прав записи в /system")
            return
        }

        toast("Установка $toolName...")

        val basePath = appDownloadsDir()?.absolutePath ?: run {
            toast("Не найдена папка с инструментами")
            return
        }

        val commands = when (toolName.lowercase()) {
            "busybox" -> listOf(
                "mount -o rw,remount /system",
                "cp $basePath/busybox $basePath/curl $basePath/openssl /system/bin/",
                "chmod 0755 /system/bin/busybox /system/bin/curl /system/bin/openssl"
            )
            "openssl" -> listOf(
                "mount -o rw,remount /system",
                "cp $basePath/openssl /system/bin/",
                "chmod 0755 /system/bin/openssl"
            )
            "gh" -> listOf(
                "mount -o rw,remount /system",
                "cp $basePath/gh /system/bin/",
                "chmod 0755 /system/bin/gh"
            )
            else -> emptyList()
        }

        if (commands.isEmpty()) {
            toast("Неизвестный инструмент: $toolName")
            return
        }

        commands.forEach { cmd ->
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                toast("Ошибка выполнения команды: $cmd")
                return
            }
        }

        toast("Установка $toolName завершена")
    }

    // endregion

    // region === Утилиты ===

    private fun toast(message: String, long: Boolean = false) {
        Toast.makeText(
            context,
            message,
            if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }

    private fun alert(message: String) {
        AlertDialog.Builder(context)
            .setTitle("Ошибка")
            .setMessage(message)
            .setPositiveButton("OK") { d, _ -> d.dismiss() }
            .create()
            .show()
    }

    /** Очистка ресивера (вызывать в onDestroy, например) */
    fun cleanup() = unregisterReceiver()

    // endregion
}