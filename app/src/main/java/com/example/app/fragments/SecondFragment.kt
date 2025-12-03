@file:Suppress("ControlFlowWithEmptyBody", "SpellCheckingInspection", "LocalVariableName")

package com.example.app.fragments

import DownloadHelper
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import java.io.DataOutputStream
import java.io.File
import java.io.IOException


@Suppress("DEPRECATION")
class SecondFragment : Fragment() {

    private val requestCodeWriteSettingsPermission = 1001
    private lateinit var downloadHelper: DownloadHelper
    private lateinit var downloadHelper2: DownloadHelper2


    fun getDownloadFolder(): File? {
        return context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        // Инициализация DownloadHelper
        downloadHelper = DownloadHelper(requireContext())
        downloadHelper2 = DownloadHelper2(requireContext())

        // Настройка кнопок
        setupButtons(view)

        return view
    }

    private fun setupButtons(view: View) {
        // Кнопка удаления пакета
        val installButton = view.findViewById<Button>(R.id.deletepkg)
        installButton.setOnClickListener { deletePkgFromFile("packages.txt") }

        // Кнопка скачивания busybox
        val downloadbusybox = view.findViewById<Button>(R.id.downloadbusybox)
        downloadbusybox.setOnClickListener { downloadBusyBox() }

        // Кнопка установки gh
        val installgh = view.findViewById<Button>(R.id.installgh)
        installgh.setOnClickListener { downloadGH() }

        // Кнопка скачивания main
        val button6 = view.findViewById<Button>(R.id.downloadmain)
        button6.setOnClickListener { downloadMain() }

        // Кнопка распаковки main
        val button7 = view.findViewById<Button>(R.id.unpackmain)
        button7.setOnClickListener { unpackMain() }

        // Кнопка установки git
        val installgit = view.findViewById<Button>(R.id.installgit)
        installgit.setOnClickListener { installGit() }

        // Кнопка установки gnupg
        val installgnupg = view.findViewById<Button>(R.id.installgnupg)
        installgnupg.setOnClickListener { installGNUPG() }

        // Кнопка скачивания  APK
        val downloadapk = view.findViewById<Button>(R.id.downloadapk)
        downloadapk.setOnClickListener { downloadAPK() }

        // Кнопка установки  APK
        val installapk = view.findViewById<Button>(R.id.installapk)
        installapk.setOnClickListener { installAPK() }

        // Кнопка установки настроек
        val setting = view.findViewById<Button>(R.id.setsettings)
        setting.setOnClickListener { setSettings() }


        // Кнопка установки OpenSSH
        val installssh = view.findViewById<Button>(R.id.installssh)
        installssh.setOnClickListener { installSSH() }


        // Кнопка установки OpenSSH LIBS
        val installsshlibs = view.findViewById<Button>(R.id.installsshlibs)
        installsshlibs.setOnClickListener { installSSHLIBS() }

        //Кнопка удаления main.tar.gz и main folder
        val deleteMain = view.findViewById<Button>(R.id.deletemain)
        deleteMain.setOnClickListener {  deleteMAIN(requireContext()) }

        //Кнопка автоматической установке apk

        val installAutoApk = view.findViewById<Button>(R.id.installautoapk)
        installAutoApk .setOnClickListener { installAutoAPK(requireContext()) }

        // Кнопка установки обоев
        val setWallaper = view.findViewById<Button>(R.id.setwallpaper)
        setWallaper.setOnClickListener { SetWallpaper() }

    }

    private fun downloadBusyBox() {
        downloadHelper.downloadTool("https://github.com/definitly486/Lenovo_TB-X304L/releases/download/busybox/busybox","busybox") { file ->
            handleDownloadResult(file, "busybox")
        }
        downloadHelper.downloadTool("https://github.com/definitly486/redmia5/releases/download/curl/curl","curl") { file ->
            handleDownloadResult(file, "curl")
        }
        downloadHelper.downloadTool("https://github.com/definitly486/Lenovo_TB-X304L/releases/download/openssl/openssl","openssl") { file ->
            handleDownloadResult(file, "openssl")
        }
    }


    private fun deleteMAIN(context: Context) {
        // Получаем приватный каталог "Загрузки"
        val privateDownloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

        // Проверяем, существует ли каталог
        if (privateDownloadsDir != null && privateDownloadsDir.exists()) {
            // Архив main.tar.gz
            val firstFile = privateDownloadsDir.resolve("main.tar.gz")
            if (firstFile.exists()) {
                if (firstFile.delete()) {
                    Toast.makeText(requireContext(), "Архив main.tar.gz успешно удалён!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Ошибка при удалении архива main.tar.gz.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Архив main.tar.gz не найден.", Toast.LENGTH_SHORT).show()
            }

            // Папка redmia5-main
            val folderToDelete = privateDownloadsDir.resolve("redmia5-main")
            if (folderToDelete.exists()) {
                if (deleteDirectory(folderToDelete)) {
                    Toast.makeText(requireContext(), "Папка 'redmia5-main' успешно удалена!", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(requireContext(), "Ошибка при удалении папки 'redmia5-main'.", Toast.LENGTH_SHORT).show()

                }
            } else {
                Toast.makeText(requireContext(), "Папка 'redmia5-main' не найдена.", Toast.LENGTH_SHORT).show()

            }
        } else {
            Toast.makeText(requireContext(), "Приватный каталог 'Загрузки' не найден.", Toast.LENGTH_SHORT).show()

        }
    }


    fun deleteDirectory(directory: File): Boolean {
        if (!directory.exists()) return false // Проверяем существование папки

        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                deleteDirectory(file) // Рекурсивно удаляем подпапки
            } else {
                file.delete() // Удаляем файлы
            }
        }

        return directory.delete() // Пробуем удалить основную папку
    }


    private fun downloadGH() {
        downloadHelper.downloadTool("https://github.com/definitly486/redmia5/releases/download/gh/gh","gh") { file ->
            handleDownloadResult(file, "gh")
        }
    }

    private fun downloadAPK() {
        CoroutineScope(Dispatchers.Main).launch {
            launchLoadSequence()
        }
    }

    private fun SetWallpaper (){
        val imagePath = "/sdcard/black_image.png" // Укажите путь к изображению

        setWallpaper(imagePath)

    }


    suspend fun launchLoadSequence() {
        val urls = listOf(
            "https://github.com/definitly486/redmia5/releases/download/apk/Total_Commander_v.3.50d.apk",
            "https://github.com/definitly486/redmia5/releases/download/apk/k9mail-13.0.apk",
            "https://github.com/definitly486/redmia5/releases/download/apk/Google+Authenticator+7.0.apk",
            "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Pluma_.private_fast.browser_1.80_APKPure.apk",
            "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/com.aurora.store_70.apk",
            "https://github.com/definitly486/redmia5/releases/download/apk/KernelSU_v1.0.5_12081-release.apk",
            "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/ByeByeDPI-arm64-v8a-release.apk",
            "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Telegram+X+0.27.5.1747-arm64-v8a.apk",
            "https://github.com/definitly486/redmia5/releases/download/apk/Core+Music+Player_1.0.apk"
        )

        urls.forEachIndexed { index, url ->
            val result = downloadSingleAPK(url)
            handleResult(result, index + 1)
        }
    }

    suspend fun downloadSingleAPK(url: String): File? {
        return suspendCancellableCoroutine { continuation ->
            downloadHelper.downloadApkToApkFolder(url) { file ->
                continuation.resumeWith(Result.success(file))
            }
        }
    }

    fun handleResult(file: File?, index: Int) {
        if (file != null) {
            Toast.makeText(
                requireContext(),
                "Файл №$index загружен: ${file.name}",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(requireContext(), "Ошибка загрузки файла №$index", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun downloadMain() {
        downloadHelper.downloadFileSimple("https://github.com/definitly486/redmia5/archive/main.tar.gz")
    }

    private fun setWallpaper(imagePath: String) {
        val tag = "WallpaperService"

        try {
            val file = File(imagePath)

            // Log the file details
            Log.d(tag, "Image Path: $imagePath")
            Log.d(tag, "File exists: ${file.exists()} and is readable: ${file.canRead()}")

            if (!file.exists()) {
                Log.e(tag, "File does not exist at $imagePath")
                return
            }

            // Optional: Check permissions if needed
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                Log.e(tag, "Permission to read external storage is not granted.")
                return
            }

            // Decode the image with options for larger images
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)

            // Calculate inSampleSize if needed
            val sampleSize = calculateInSampleSize(options, 100, 100)
            options.inSampleSize = sampleSize
            options.inJustDecodeBounds = false

            val bitmap: Bitmap? = BitmapFactory.decodeFile(imagePath, options)

            if (bitmap == null) {
                Log.e(tag, "Failed to decode bitmap from $imagePath. The file might be corrupted or in an unsupported format.")
                return
            }

            val wallpaperManager = WallpaperManager.getInstance(requireContext())
            wallpaperManager.setBitmap(bitmap)

            Log.i(tag, "Wallpaper set successfully")
        } catch (e: IOException) {
            Log.e(tag, "IOException while setting wallpaper: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(tag, "Unexpected error while setting wallpaper: ${e.message}")
            e.printStackTrace()
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun installAPK() {
        val urls = listOf(
            "Total_Commander_v.3.50d.apk",
            "k9mail-13.0.apk",
            "Google+Authenticator+7.0.apk",
            "Pluma_.private_fast.browser_1.80_APKPure.apk",
            "com.aurora.store_70.apk",
            "KernelSU_v1.0.5_12081-release.apk",
            "ByeByeDPI-arm64-v8a-release.apk",
            "Telegram+X+0.27.5.1747-arm64-v8a.apk",
            "Core+Music+Player_1.0.apk"
        )

        for (url in urls) {
            context?.getExternalFilesDir("APK")?.also { it.mkdirs() }

            downloadHelper.installApkFromApkFolder(url)
        }
    }

    private fun setSettings() {
        val TAG = "SETTINGS_APPLY"  // Основной тег для фильтрации в Logcat

        // Анонимный объект для выполнения shell-команд с улучшенным логированием
        val shellExecutor = object {
            fun execShellCommand(command: String): Boolean {
                val shortCmd = if (command.length > 80) command.substring(0, 77) + "..." else command
                Log.i(TAG, "Выполняю команду: $shortCmd")

                var process: Process? = null
                var outputStream: DataOutputStream? = null
                return try {
                    process = Runtime.getRuntime().exec("su")
                    outputStream = DataOutputStream(process.outputStream)
                    outputStream.writeBytes("$command\n")
                    outputStream.writeBytes("exit\n")
                    outputStream.flush()
                    outputStream.close()

                    val exitCode = process.waitFor()
                    if (exitCode == 0) {
                        Log.d(TAG, "УСПЕШНО: $shortCmd")
                    } else {
                        Log.w(TAG, "ОШИБКА (exit=$exitCode): $shortCmd")
                    }
                    exitCode == 0
                } catch (e: Exception) {
                    Log.e(TAG, "ИСКЛЮЧЕНИЕ при выполнении: $shortCmd", e)
                    false
                } finally {
                    try { outputStream?.close() } catch (_: Exception) {}
                    process?.destroy()
                }
            }
        }

        // Проверка и запрос WRITE_SETTINGS
        if (!Settings.System.canWrite(requireContext())) {
            Log.w(TAG, "Нет разрешения WRITE_SETTINGS — запрашиваем у пользователя")
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            }
            startActivityForResult(intent, requestCodeWriteSettingsPermission)
            return
        } else {
            Log.i(TAG, "Разрешение WRITE_SETTINGS уже есть — начинаем применять настройки")
        }

        // === ВЫПОЛНЯЕМ ВСЕ НАСТРОЙКИ С ЛОГИРОВАНИЕМ ===
        shellExecutor.execShellCommand("appops set com.example.app  REQUEST_INSTALL_PACKAGES allow")
        shellExecutor.execShellCommand("pm grant com.example.app android.permission.WRITE_SECURE_SETTINGS")
        shellExecutor.execShellCommand("pm grant com.example.app  android.permission.WRITE_SETTINGS")

        // Яркость
        try {
            setScreenBrightness(requireContext(), 400) // 800 — это слишком много, максимум 255!
            Log.i(TAG, "Яркость установлена вручную (230/400)")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка установки яркости", e)
        }

        // Включаем режим разработчика и ADB
        shellExecutor.execShellCommand("settings put global development_settings_enabled 1")
        shellExecutor.execShellCommand("settings put global adb_enabled 1")

        // КРИТИЧЕСКИ ВАЖНО: замена нерабочей команды на рабочую!
        shellExecutor.execShellCommand("settings put global adb_wifi_enabled 1 ")


        // Навигационная панель (жесты)
        shellExecutor.execShellCommand("cmd overlay enable com.android.internal.systemui.navbar.gestural")

        // Подключение к Wi-Fi сетям
        shellExecutor.execShellCommand("cmd wifi connect-network HUAWEI-B315-AFCA wpa2 HR63B1DMTJ4")
        shellExecutor.execShellCommand("cmd wifi connect-network 32 wpa2 9175600380")

        //установить безвук
        shellExecutor.execShellCommand("cmd audio set-ringer-mode SILENT")

        // Тёмная тема
        shellExecutor.execShellCommand("cmd uimode night yes")

        // 120 Гц (если поддерживается железом)
        shellExecutor.execShellCommand("settings put system min_refresh_rate 120.0")
        shellExecutor.execShellCommand("settings put system peak_refresh_rate 120.0")

        // Установка из неизвестных источников
        shellExecutor.execShellCommand("settings put secure install_non_market_apps 1")

        //очистка рабочено стола
        shellExecutor.execShellCommand("pm clear com.gogo.launcher")

        // Отключаем Bluetooth
        shellExecutor.execShellCommand("cmd bluetooth_manager disable")

        // Отключаем автояркость
        try {
            val result = Settings.System.putInt(
                requireContext().contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Log.i(TAG, "Автояркость выключена: $result")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка отключения автояркости", e)
        }

        Log.i(TAG, "Все настройки применены!")
    }

    fun setScreenBrightness(context: Context, brightnessValue: Int) {
        if (brightnessValue in 0..1000) {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )

            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightnessValue
            )
        }
    }


    private fun unpackMain() {
        val folder = getDownloadFolder() ?: return
        val tarGzFile = File(folder, "main.tar.gz")
        val outputDir = File(folder, "")
        if (!tarGzFile.exists()) {
            Toast.makeText(requireContext(), "Файл main.tar.gz не существует", Toast.LENGTH_SHORT).show()
            return
        }
        downloadHelper2 = DownloadHelper2(requireContext())
        downloadHelper2.decompressTarGz(tarGzFile, outputDir)
        Thread.sleep(5000L)
        downloadHelper2.copymain()
    }

    private fun installGit() {
        val folder = getDownloadFolder() ?: return
        val tarGzFile = File(folder, "git_aarch64.tar.xz")
        val outputDir = File(folder, "")
        if (!tarGzFile.exists()) {
            Toast.makeText(requireContext(), "Файл git_aarch64.tar.xz не существует", Toast.LENGTH_SHORT).show()
            downloadHelper.downloadFileSimple("https://github.com/definitly486/redmia5/releases/download/git/git_aarch64.tar.xz")
            return
        }
        downloadHelper2 = DownloadHelper2(requireContext())
        downloadHelper2.unpackTarXz(tarGzFile, outputDir)
        Thread.sleep(3000L)
        downloadHelper2.copygit()
    }

    private fun installSSH() {
        val folder = getDownloadFolder() ?: return
        val tarGzFile = File(folder, "openssh_bin.tar.xz")
        val outputDir = File(folder, "")
        if (!tarGzFile.exists()) {
            Toast.makeText(requireContext(), "Файл openssh_bin.tar.xz не существует", Toast.LENGTH_SHORT).show()
            downloadHelper.downloadFileSimple("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/openssh/openssh_bin.tar.xz")

            return
        }
        downloadHelper2 = DownloadHelper2(requireContext())
        downloadHelper2.unpackTarXz(tarGzFile, outputDir)
        Thread.sleep(3000L)
        downloadHelper2.copyssh()
    }

    private fun installSSHLIBS() {
        val folder = getDownloadFolder() ?: return
        val tarGzFile = File(folder, "openssh_libs.tar.xz")
        val outputDir = File(folder, "")
        if (!tarGzFile.exists()) {
            Toast.makeText(requireContext(), "Файл openssh_libs.tar.xz не существует", Toast.LENGTH_SHORT).show()
            downloadHelper.downloadFileSimple("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/openssh/openssh_libs.tar.xz")

            return
        }
        downloadHelper2 = DownloadHelper2(requireContext())
        downloadHelper2.unpackTarXz(tarGzFile, outputDir)
        Thread.sleep(3000L)
        downloadHelper2.copysshlibs()
    }

    private fun installGNUPG() {
        val folder = getDownloadFolder() ?: return
        val tarGzFile = File(folder, "gnupg_aarch64.tar.xz")
        val outputDir = File(folder, "")
        if (!tarGzFile.exists()) {
            Toast.makeText(requireContext(), "Файл gnupg_aarch64.tar.xz не существует", Toast.LENGTH_SHORT).show()
            downloadHelper.downloadFileSimple("https://github.com/definitly486/redmia5/releases/download/gnupg/gnupg_aarch64.tar.xz")
            return
        }
        downloadHelper2 = DownloadHelper2(requireContext())
        downloadHelper2.unpackTarXz(tarGzFile, outputDir)
        Thread.sleep(3000L)
        downloadHelper2.copygnupg()
    }

    private fun handleDownloadResult(file: File?, name: String) {
        if (file != null) {
            Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Ошибка загрузки файла $name", Toast.LENGTH_SHORT).show()
        }
    }


    @Suppress("PrivatePropertyName")
    private val TAG = "PkgDeleter"  // Твоя метка для фильтрации в Logcat

    fun Fragment.deletePkgFromFile(fileName: String) {
        if (!RootChecker.hasRootAccess(requireContext())) {
            showCompletionDialogroot(requireContext())
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Начинается удаление пакетов...", Toast.LENGTH_SHORT).show()
            }

            Log.i(TAG, "Начало массового удаления из файла: $fileName")

            val appPrivateDirectory = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: run {
                    val error = "Не удалось получить папку для файлов"
                    Log.e(TAG, error)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

            val file = File(appPrivateDirectory, fileName)
            if (!file.exists()) {
                Log.w(TAG, "Файл не найден: $fileName")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Файл не найден: $fileName", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            Log.i(TAG, "Найден файл: ${file.absolutePath}, строк: ${file.readLines().size}")

            var processedCount = 0
            var successCount = 0
            var skippedCount = 0

            try {
                val lines = file.readLines()

                for ((index, rawLine) in lines.withIndex()) {
                    val packageName = rawLine.trim()
                    if (packageName.isBlank()) {
                        Log.v(TAG, "Пропущена пустая строка #${index + 1}")
                        continue
                    }

                    processedCount++
                    Log.i(TAG, "Обработка [$processedCount/${lines.size}]: $packageName")

                    // Проверка установки
                    val isInstalled = withContext(Dispatchers.Main) {
                        isPackageInstalled(packageName)
                    }

                    if (!isInstalled) {
                        Log.w(TAG, "Пропущен (не установлен): $packageName")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Пропущен: $packageName", Toast.LENGTH_SHORT).show()
                        }
                        skippedCount++
                        continue
                    }

                    // Удаление
                    val deleted = deletePackageRoot(packageName)

                    if (deleted) {
                        Log.i(TAG, "УСПЕШНО удалён: $packageName")
                        successCount++
                    } else {
                        Log.e(TAG, "ОШИБКА при удалении: $packageName")
                    }

                    delay(500) // Пауза для стабильности
                }

                // Итог
                val summary = "Завершено! Обработано: $processedCount | Удалено: $successCount | Пропущено: $skippedCount"
                Log.i(TAG, summary)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), summary, Toast.LENGTH_LONG).show()
                    showCompletionDialog(requireContext())
                    createReloadDialog()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Критическая ошибка при обработке файла", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Проверка установки пакета
    private fun Fragment.isPackageInstalled(packageName: String): Boolean {
        val pm = requireContext().packageManager
        return try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    // API 33+: используем MATCH_ALL через флаги
                    pm.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(PackageManager.MATCH_ALL.toLong())
                    )
                    true
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> { // API 30+
                    // На API 30–32 используем новый метод с int-флагами
                    pm.getPackageInfo(packageName, PackageManager.MATCH_ALL)
                    true
                }
                else -> {
                    // До API 30: старый способ (депрекейтед, но работает)
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES)
                    // Или просто 0 — но с 0 системные тоже могут не находиться на некоторых устройствах
                    true
                }
            }
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при проверке пакета $packageName", e)
            false
        }
    }

    // Удаление через root
    private fun deletePackageRoot(packageName: String): Boolean {
        return try {
            Log.d(TAG, "Выполняется: su -c pm uninstall $packageName")
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "pm uninstall --user 0  $packageName"))
            val exitCode = process.waitFor()
            val success = exitCode == 0
            if (success) {
                Log.i(TAG, "Команда выполнена успешно (exit code: ${0})")
            } else {
                Log.e(TAG, "Команда завершилась с ошибкой (exit code: $exitCode)")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при выполнении su-команды для $packageName", e)
            false
        }
    }

    fun showCompletionDialogroot(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Проверка root")
        builder.setMessage("Root доступ отсуствует,приложения не будут удалены")
        builder.setPositiveButton("Продолжить") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    fun showCompletionDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Удаление завершено")
        builder.setMessage("Все выбранные пакеты успешно удалены.")
        builder.setPositiveButton("Продолжить") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    fun rebootDevice() {
        try {
            val runtime = Runtime.getRuntime()
            runtime.exec("su -c reboot")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createReloadDialog() {
        val alertBuilder = AlertDialog.Builder(requireContext())

        // Заголовок диалога
        alertBuilder.setTitle("Подтверждение перезагрузки")

        // Сообщение в диалоговом окне
        alertBuilder.setMessage("Вы действительно хотите перезагрузить устройство?")

        // Положительная кнопка (перезагружаем устройство)
        alertBuilder.setPositiveButton("Да") { _: DialogInterface, _: Int ->
            // Логика перезагрузки устройства (нужны права администратора или root)

            rebootDevice() }

        // Отрицательная кнопка (закрываем диалог)
        alertBuilder.setNegativeButton("Нет") { dialog: DialogInterface, _: Int ->
            dialog.cancel()
        }

        // Показываем диалог
        val dialog = alertBuilder.create()
        dialog.show()
    }



    // Лог-тег, используемый для идентификации сообщений нашего приложения
    @Suppress("PrivatePropertyName")
    private val LOG_TAG = "InstallAutoAPK"

    private fun installAutoAPK(context: Context?) {
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