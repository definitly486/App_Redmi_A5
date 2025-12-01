@file:Suppress("SpellCheckingInspection", "unused", "DEPRECATION",
    "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
)

package com.example.app

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.app.fragments.RootChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


// Вспомогательные функции

// Получение директории загрузки
fun getDownloadFolder(context: Context): File? {
    return context.getExternalFilesDir("shared")
}

// Скачивание файла
fun download(context: Context, url: String) {
    val folder = getDownloadFolder(context) ?: return
    if (!folder.exists()) folder.mkdirs()

    val lastPart = url.split("/").last()
    val gpgFile = File(folder, lastPart)

    if (gpgFile.exists()) {
        Toast.makeText(context, "Файл уже существует", Toast.LENGTH_SHORT).show()
        return
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Начинается загрузка...", Toast.LENGTH_SHORT).show()
            }

            val request = DownloadManager.Request(Uri.parse(url))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setTitle(lastPart)
            request.setDescription("Загружается...")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalFilesDir(
                context,
                "shared",  // Папка "shared"
                lastPart
            )

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

        } catch (ex: Exception) {
            ex.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Ошибка при загрузке: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

// Функция расшифровки и распаковки архива
suspend fun decryptAndExtractArchive(context: Context, archiveName: String, password: String) {
    val folder = context.getExternalFilesDir("shared")
    val encryptedFilePath = "${folder!!.absolutePath}/${archiveName}.tar.enc"
    val decryptedFilePath = "${folder.absolutePath}/${archiveName}.tar"
    val appDirectoryPath = folder.absolutePath

    if (!folder.exists()) {
        folder.mkdirs()
    }

    try {
        // Расшифровка файла
        val processDecrypt = ProcessBuilder(
            "openssl",
            "enc",
            "-aes-256-cbc",
            "-pbkdf2",
            "-iter",
            "100000",
            "-d",
            "-in",
            encryptedFilePath,
            "-out",
            decryptedFilePath,
            "-pass",
            "pass:$password"
        ).start()

        processDecrypt.waitFor()

        if (!File(decryptedFilePath).exists()) {
            throw RuntimeException("Расшифровка прошла неудачно.")
        }

        // Распаковка архива
        val processUnpack = ProcessBuilder(
            "busybox",
            "tar",
            "xf",
            decryptedFilePath,
            "-C",
            appDirectoryPath
        ).start()

        copyprofile(context,archiveName)
        processUnpack.waitFor()

        withContext(Dispatchers.Main) {
            showToastOnMainThread(context, "Архив успешно распакован!")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        withContext(Dispatchers.Main) {
            showToastOnMainThread(context, "Ошибка при распаковке: ${e.message}")
        }
    }
}

//Расшифровка с испоьзованием библиотек
fun CoroutineScope.decryptWithOpenSslFormat2(context: Context, archiveName: String, password: String) {
    launch(Dispatchers.IO) {
        val folder = context.getExternalFilesDir("shared")
        folder?.let { dir ->
            val encryptedFilePath = "${dir.absolutePath}/${archiveName}.tar.enc"
            val decryptedFilePath = "${dir.absolutePath}/${archiveName}.tar"
            val appDirectoryPath = dir.absolutePath

            if (!dir.exists()) {
                dir.mkdirs()
            }

            try {
                // Используем FileInputStream для чтения файла по абсолютному пути
                FileInputStream(File(encryptedFilePath)).use { input ->
                    FileOutputStream(decryptedFilePath).use { fileOut ->

                        // 1. Чтение заголовка "Salted__"
                        val header = ByteArray(8)
                        if (input.read(header) != 8 || !header.contentEquals("Salted__".toByteArray())) {
                            throw IllegalArgumentException("Файл не в формате OpenSSL (нет 'Salted__')")
                        }

                        // 2. Чтение соли (8 байт)
                        val salt = ByteArray(8)
                        if (input.read(salt) != 8) throw IOException("Не удалось прочитать соль")

                        // 3. Генерация ключа (32 байта) и вектора инициализации (IV, 16 байт) через PBKDF2-HMAC-SHA256
                        val (key, iv) = deriveKeyAndIvWithPbkdf2(password, salt)

                        // 4. Расшифровка
                        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
                            init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
                        }

                        CipherOutputStream(fileOut, cipher).use { cipherOut ->
                            input.copyTo(cipherOut)
                        }
                    }
                }

                // Распаковка tar архива
                TarArchiveInputStream(BufferedInputStream(FileInputStream(decryptedFilePath))).use { tarIn ->
                    var entry: TarArchiveEntry? = tarIn.nextTarEntry
                    while (entry != null) {
                        val currentEntry = entry.name
                        val file = File(appDirectoryPath, currentEntry)

                        when {
                            entry.isDirectory -> {
                                if (!file.exists()) {
                                    file.mkdirs()
                                }
                            }
                            else -> {
                                file.parentFile.mkdirs()
                                FileOutputStream(file).use { out ->
                                    tarIn.copyTo(out)
                                }
                            }
                        }
                        entry = tarIn.nextTarEntry
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                throw IOException("Ошибка при обработке файла: ${ex.message}")
            }
        } ?: run {
            println("Каталог shared не найден.")
        }
    }
        //Функция копировая профиля.
    copyprofile(context,archiveName)

}

private fun deriveKeyAndIvWithPbkdf2(password: String, salt: ByteArray): Pair<ByteArray, ByteArray> {
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val spec = PBEKeySpec(password.toCharArray(), salt, 100000, 384) // 384 бита = 48 байт
    val key = factory.generateSecret(spec).encoded

    val aesKey = ByteArray(32)
    val iv = ByteArray(16)
    System.arraycopy(key, 0, aesKey, 0, 32)
    System.arraycopy(key, 32, iv, 0, 16)

    return aesKey to iv
}




// Универсальный метод копирования профиля
fun copyprofile(context: Context, appPackageName: String) {
    val folder = context.getExternalFilesDir("shared")



    fun showCompletionDialogsystem() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Проверка записи в system")
        builder.setMessage("Запись в system невозможна, приложения не будут установлены")
        builder.setPositiveButton("Продолжить") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    // Проверка возможности записи в папку '/system'
    val pathToCheck = "/system"
    if (!RootChecker.checkWriteAccess(pathToCheck)) {
        showCompletionDialogsystem()
        return
    }


    if (appPackageName == "org.thunderdog.challegram") {

        "su - root -c   rm   /data_mirror/data_ce/null/0/$appPackageName/files/tdlib/td.binlog"

    }


    val ownerCmd =
        "su - root -c   ls -l   /data_mirror/data_ce/null/0/ | grep $appPackageName |  awk '{print$3}'"
    val fileOwner = execShell(ownerCmd)?.trim() ?: ""
    showToastOnMainThread(context, "ID $fileOwner")


    if (appPackageName == "com.google.android.apps.authenticator2") {

        val commands = arrayOf(
            "su - root -c cp  -R ${folder!!.absolutePath}/$appPackageName/files/accounts  /data_mirror/data_ce/null/0/$appPackageName/files/",
            "su - root -c chown -R   $fileOwner:$fileOwner   /data_mirror/data_ce/null/0/$appPackageName/"
        )
    }




    if (appPackageName == "org.thunderdog.challegram") {

        val commands = arrayOf(
            "su - root -c cp  -R ${folder!!.absolutePath}/$appPackageName/files/accounts  /data_mirror/data_ce/null/0/$appPackageName/files/ ",
            "su - root -c chown -R   $fileOwner:$fileOwner   /data_mirror/data_ce/null/0/$appPackageName/"
        )
    }


    val commands = arrayOf(
        "su - root -c cp  -R ${folder!!.absolutePath}/$appPackageName/  /data_mirror/data_ce/null/0/ ",
        "su - root -c chown -R  $fileOwner:$fileOwner  /data_mirror/data_ce/null/0/$appPackageName/"
    )

    for (command in commands) {
        CoroutineScope(Dispatchers.IO).launch {

            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            if (process.exitValue() != 0) {
                showToastOnMainThread(context, "Ошибка при копировании $appPackageName: $command")
                return@launch
            }
        }
    }

    showToastOnMainThread(context, "Копирование $appPackageName завершено")
}

private fun execShell(cmd: String): String? {
    try {
        val process = Runtime.getRuntime().exec(cmd)
        process.waitFor()
        if (process.exitValue() != 0) {
            throw Exception("Ошибка при выполнении команды: $cmd")
        }

        val outputStream = BufferedReader(InputStreamReader(process.inputStream))
        val resultBuilder = StringBuilder()
        while (true) {
            val line = outputStream.readLine() ?: break
            resultBuilder.append(line).append("\n")
        }
        return resultBuilder.toString().trim()
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

// Вспомогательная функция для показа Toast на главном потоке
fun showToastOnMainThread(context: Context, message: String) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}