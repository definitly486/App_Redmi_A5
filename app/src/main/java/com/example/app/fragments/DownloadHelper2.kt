@file:Suppress("SameParameterValue", "SpellCheckingInspection")

package com.example.app.fragments

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStreamReader


@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class DownloadHelper2(private val context: Context) {
    val folder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    fun decompressTarGz(tarGzFile: File, outputDir: File) {

        // Ensure canonical path for security
        outputDir.canonicalFile

        if (!tarGzFile.exists()) throw FileNotFoundException("File not found: ${tarGzFile.path}") as Throwable
        GzipCompressorInputStream(BufferedInputStream(FileInputStream(tarGzFile))).use { gzIn ->
            TarArchiveInputStream(gzIn).use { tarIn ->
                generateSequence { tarIn.nextEntry }.forEach { entry ->

                    val outputFile = File(outputDir, entry.name).canonicalFile

                    // Check if the extracted file stays inside outputDir
                    // Prevent Zip Slip Vulnerability
                    // if (!outputFile.toPath().startsWith(canonicalOutputDir.toPath())) {
                    //     throw SecurityException("Zip Slip vulnerability detected! Malicious entry: ${entry.name}")
                    //  }

                    if (entry.isDirectory) outputFile.mkdirs()
                    else {
                        outputFile.parentFile.mkdirs()
                        outputFile.outputStream().use { outStream ->
                            tarIn.copyTo(outStream)
                        }
                    }
                }
            }
        }
    }


    fun unpackTarXz(tarXzFile: File, outputDirectory: File) {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }

        FileInputStream(tarXzFile).use { fis ->
            BufferedInputStream(fis).use { bis ->
                XZCompressorInputStream(bis).use { xzIn ->
                    TarArchiveInputStream(xzIn).use { tarIn ->
                        var entry = tarIn.nextEntry
                        while (entry != null) {
                            val outputFile = File(outputDirectory, entry.name)
                            if (entry.isDirectory) {
                                outputFile.mkdirs()
                            } else {
                                FileOutputStream(outputFile).use { fos ->
                                    tarIn.copyTo(fos)
                                }
                            }
                            entry = tarIn.nextEntry
                        }
                    }
                }
            }
        }

    }


    fun copymain() {


        fun showCompletionDialoginstall() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка root")
            builder.setMessage("Root доступ отсуствует,приложения не будут установлены")
            builder.setPositiveButton("Продолжить") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }


        if (RootChecker.hasRootAccess(context)) {

            Toast.makeText(context, "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
                .show()
        } else {
            showCompletionDialoginstall()
            return
        }



        Toast.makeText(context, "Копируем redmia5-main ...", Toast.LENGTH_SHORT).show()


        val prepareCommands =
            arrayOf("su - root -c chmod -R 0755 $folder/redmia5-main")
        for (command in prepareCommands) {
            Runtime.getRuntime().exec(command).waitFor()
        }

        val ownerCmd =
            "su - root -c   ls -l   /data_mirror/data_ce/null/0/com.termos | awk '{print $3}' | head -n 2"
        val fileOwner = execShell(ownerCmd)?.trim() ?: ""

        val commands = arrayOf(

            "su - root -c cp  -R $folder/redmia5-main /data_mirror/data_ce/null/0/com.termos/files/home",
            "su - root -c chmod -R 0755 /data_mirror/data_ce/null/0/com.termos/files/home",
            "su - root -c chown -R  $fileOwner:$fileOwner /data_mirror/data_ce/null/0/com.termos/files/home/redmia5-main"
        )

        var process: Process?

        for (command in commands) {
            process = Runtime.getRuntime().exec(command)
            process.waitFor() // Wait for the command to finish
            if (process.exitValue() != 0) {
                Toast.makeText(context, "Ошибка при копирование main: $command", Toast.LENGTH_LONG)
                    .show()
                return
            }
        }
        Toast.makeText(context, "Копирование  main завершенo", Toast.LENGTH_SHORT).show()
    }

    fun installpippython3(){
        fun showCompletionDialoginstall() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка root")
            builder.setMessage("Root доступ отсуствует,pip не будет установлен")
            builder.setPositiveButton("Продолжить") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }


        if (RootChecker.hasRootAccess(context)) {

            Toast.makeText(context, "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
                .show()
        } else {
            showCompletionDialoginstall()
            return
        }


        fun showCompletionDialogsystem() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка записи в system")
            builder.setMessage("Запись в system не возможна, pip не будут установлен")
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

val pipScript = """
    #!/system/bin/sh
if [ "${'$'}(id -u)" -ne 0 ]; then
    echo "Ошибка: этот скрипт должен запускаться от root" >&2
    exit 1
fi
    source /data/local/tmp/env/bin/activate
    pip "$@"
""".trimIndent()

try {
    val file = File("$folder/pip")
    file.writeText(pipScript)
    file.setExecutable(true)  // chmod +x
    Toast.makeText(context, "pip успешно создан!", Toast.LENGTH_SHORT).show()
} catch (e: Exception) {
    Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
}


        Toast.makeText(context, "Установка pip Python3 ...", Toast.LENGTH_SHORT).show()
        val commands = arrayOf(
            "su - root -c mount -o rw,remount /system",
            "su - root -c cp $folder/pip /system/bin",
            "su - root -c chmod +x /system/bin/pip",
            "su - root -c chmod 0755 /system/bin/pip"

        )

        var process: Process?

        for (command in commands) {
            process = Runtime.getRuntime().exec(command)
            process.waitFor() // Wait for the command to finish
            if (process.exitValue() != 0) {
                Toast.makeText(context, "Ошибка при создание yt-dlp: $command", Toast.LENGTH_LONG)
                    .show()
                return
            }
        }
        Toast.makeText(context, "Создание yt-dlp завершено", Toast.LENGTH_SHORT)
    }

    fun installenvpython3(){

        fun showCompletionDialoginstall() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка root")
            builder.setMessage("Root доступ отсуствует,окружение  не будут установлено")
            builder.setPositiveButton("Продолжить") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        if (RootChecker.hasRootAccess(context)) {

            Toast.makeText(context, "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
                .show()
        } else {
            showCompletionDialoginstall()
            return
        }

        Toast.makeText(context, "Устанваливаем виртальное окружение Python3 ...", Toast.LENGTH_SHORT).show()
        val commands = arrayOf(
            "su - root -c python3  -m venv  /data/local/tmp/env",
            "su - root -c chmod +x /data/local/tmp/env/bin/pip",
            "su - root -c chmod  -R 0755 /data/local/tmp/env/bin/pip",
            "su - root -c chcon -R  u:object_r:system_file:s0 /data/local/tmp/env/bin/pip"
            )

        var process: Process?

        for (command in commands) {
            process = Runtime.getRuntime().exec(command)
            process.waitFor() // Wait for the command to finish
            if (process.exitValue() != 0) {
                Toast.makeText(context, "Ошибка при установке виртуального окружения Python3: $command", Toast.LENGTH_LONG)
                    .show()
                return
            }
        }
        Toast.makeText(context, "Устанвка виртуального окружения  Python3 завершенo", Toast.LENGTH_SHORT).show()

    }


    fun copypython3() {

        fun showCompletionDialoginstall() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка root")
            builder.setMessage("Root доступ отсуствует,приложения не будут установлены")
            builder.setPositiveButton("Продолжить") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }


        if (RootChecker.hasRootAccess(context)) {

            Toast.makeText(context, "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
                .show()
        } else {
            showCompletionDialoginstall()
            return
        }


        fun showCompletionDialogsystem() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка записи в system")
            builder.setMessage("Запись в system не возможна, приложения не будут установлены")
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

        Toast.makeText(context, "Копируем Python3 ...", Toast.LENGTH_SHORT).show()
        val commands = arrayOf(
              "su - root -c  mount -o rw,remount /system",
            "su - root -c cp  -R  $folder/python-android-aarch64 /data/local/tmp/ ",
            "su - root -c cp  -R  $folder/python-android-aarch64/python3 /system/bin ",
            "su - root -c chmod -R 0755 /system/bin/python3",
            "su - root -c chmod +x /data/local/tmp/python-android-aarch64/bin/python3.13",
            "su - root -c chcon u:object_r:system_file:s0  /data/local/tmp/python-android-aarch64/bin/python3.13",
            "su - root -c chcon -R  u:object_r:system_file:s0 /data/local/tmp/python-android-aarch64",
            "su - root -c chmod  -R 0755 /data/local/tmp/",
            "su - root -c chmod +x /system/bin/python3",
            "su - root -c  chcon u:object_r:system_file:s0 /system/bin/python3"
         )

        var process: Process?

        for (command in commands) {
            process = Runtime.getRuntime().exec(command)
            process.waitFor() // Wait for the command to finish
            if (process.exitValue() != 0) {
                Toast.makeText(context, "Ошибка при копирование Python3: $command", Toast.LENGTH_LONG)
                    .show()
                return
            }
        }
        Toast.makeText(context, "Копирование   Python3 завершенo", Toast.LENGTH_SHORT).show()
    }


    fun copyssh() {


        fun showCompletionDialoginstall() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка root")
            builder.setMessage("Root доступ отсуствует,приложения не будут установлены")
            builder.setPositiveButton("Продолжить") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }


        if (RootChecker.hasRootAccess(context)) {

            Toast.makeText(context, "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
                .show()
        } else {
            showCompletionDialoginstall()
            return
        }


        fun showCompletionDialogsystem() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка записи в system")
            builder.setMessage("Запись в system не возможна, приложения не будут установлены")
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
        Toast.makeText(context, "Копируем OpenSSH ...", Toast.LENGTH_SHORT).show()


        val commands = arrayOf(
            "su - root -c  mount -o rw,remount /system",
            "su - root -c cp   $folder/openssh_bin/bin/rsync /system/bin/ ",
            "su - root -c cp   $folder/openssh_bin/bin/scp /system/bin/ ",
            "su - root -c cp   $folder/openssh_bin/bin/sftp /system/bin/ ",
            "su - root -c cp   $folder/openssh_bin/bin/ssh /system/bin/ ",
            "su - root -c cp   $folder/openssh_bin/bin/ssh-keygen /system/bin/ ",
            "su - root -c chmod -R 0755 /system/bin/rsync",
            "su - root -c chmod -R 0755 /system/bin/scp",
            "su - root -c chmod -R 0755 /system/bin/sftp",
            "su - root -c chmod -R 0755 /system/bin/ssh",
            "su - root -c chmod -R 0755 /system/bin/ssh-keygen",
            "su - root -c chmod +x /system/bin/rsync",
            "su - root -c chmod +x /system/bin/scp",
            "su - root -c chmod +x /system/bin/sftp",
            "su - root -c chmod +x /system/bin/ssh",
            "su - root -c chmod +x /system/bin/ssh-keygen"

        )

        var process: Process?

        for (command in commands) {
            process = Runtime.getRuntime().exec(command)
            process.waitFor() // Wait for the command to finish
            if (process.exitValue() != 0) {
                Toast.makeText(context, "Ошибка при копирование OpenSSH: $command", Toast.LENGTH_LONG)
                    .show()
                return
            }
        }
        Toast.makeText(context, "Копирование   OpenSSH завершенo", Toast.LENGTH_SHORT).show()
    }


    fun copysshlibs() {


        fun showCompletionDialoginstall() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка root")
            builder.setMessage("Root доступ отсуствует,приложения не будут установлены")
            builder.setPositiveButton("Продолжить") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }


        if (RootChecker.hasRootAccess(context)) {

            Toast.makeText(context, "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
                .show()
        } else {
            showCompletionDialoginstall()
            return
        }


        fun showCompletionDialogsystem() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка записи в system")
            builder.setMessage("Запись в system не возможна, приложения не будут установлены")
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



        Toast.makeText(context, "Копируем SSH LIBS ...", Toast.LENGTH_SHORT).show()


        val commands = arrayOf(
            "su - root -c  mount -o rw,remount /system",
            "su - root -c cp   $folder/openssh_libs/libcrypto.so.1.0.0 /system/lib64/ ",
            "su - root -c chmod -R 0755 /system/lib64/libcrypto.so.1.0.0 ",
            "su - root -c chmod -R  0755 /system/lib64/"
        )

        var process: Process?

        for (command in commands) {
            process = Runtime.getRuntime().exec(command)
            process.waitFor() // Wait for the command to finish
            if (process.exitValue() != 0) {
                Toast.makeText(context, "Ошибка при копирование SSH LIBS: $command", Toast.LENGTH_LONG)
                    .show()
                return
            }
        }
        Toast.makeText(context, "Копирование  SSH LIBS завершенo", Toast.LENGTH_SHORT).show()
    }

    fun copygit() {


        fun showCompletionDialoginstall() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка root")
            builder.setMessage("Root доступ отсуствует,приложения не будут установлены")
            builder.setPositiveButton("Продолжить") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }


        if (RootChecker.hasRootAccess(context)) {

            Toast.makeText(context, "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
                .show()
        } else {
            showCompletionDialoginstall()
            return
        }


        fun showCompletionDialogsystem() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка записи в system")
            builder.setMessage("Запись в system не возможна, приложения не будут установлены")
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



        Toast.makeText(context, "Копируем GIT ...", Toast.LENGTH_SHORT).show()


        val commands = arrayOf(
            "su - root -c  mount -o rw,remount /system",
            "su - root -c cp   $folder/git/git /system/bin/ ",
            "su - root -c cp   $folder/git/libcrypto.so.3 /system/lib64/ ",
            "su - root -c cp   $folder/git/libiconv.so /system/lib64/ ",
            "su - root -c cp   $folder/git/libpcre2-8.so /system/lib64/ ",
            "su - root -c cp   $folder/git/libz.so.1 /system/lib64/ ",
            "su - root -c chmod -R 0755 /system/lib64/libpcre2-8.so",
            "su - root -c chmod -R 0755 /system/lib64/libz.so.1",
            "su - root -c chmod -R 0755 /system/lib64/libiconv.so",
            "su - root -c chmod -R 0755 /system/lib64/libcrypto.so.3",
            "su - root -c chmod +x /system/bin/git",
        )

        var process: Process?

        for (command in commands) {
            process = Runtime.getRuntime().exec(command)
            process.waitFor() // Wait for the command to finish
            if (process.exitValue() != 0) {
                Toast.makeText(context, "Ошибка при копирование main: $command", Toast.LENGTH_LONG)
                    .show()
                return
            }
        }
        Toast.makeText(context, "Копирование  main завершенo", Toast.LENGTH_SHORT).show()
    }

    fun copygnupg() {


        fun showCompletionDialoginstall() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка root")
            builder.setMessage("Root доступ отсуствует,приложения не будут установлены")
            builder.setPositiveButton("Продолжить") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }


        if (RootChecker.hasRootAccess(context)) {

            Toast.makeText(context, "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
                .show()
        } else {
            showCompletionDialoginstall()
            return
        }

        fun showCompletionDialogsystem() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка записи в system")
            builder.setMessage("Запись в system не возможна, приложения не будут установлены")
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


        Toast.makeText(context, "Копируем GnuPG ...", Toast.LENGTH_SHORT).show()

        val commands = arrayOf(
            "su - root -c  mount -o rw,remount /system",
            "su - root -c cp   $folder/gnupg/gpg /system/bin/",
            "su - root -c cp   $folder/gnupg/libcrypto.so.3 /system/lib64/",
            "su - root -c cp   $folder/gnupg/libiconv.so /system/lib64/",
            "su - root -c cp   $folder/gnupg/libpcre2-8.so /system/lib64/",
            "su - root -c cp   $folder/gnupg/libz.so.1 /system/lib64/",
            "su - root -c cp   $folder/gnupg/libandroid-support.so /system/lib64/",
            "su - root -c cp   $folder/gnupg/libassuan.so /system/lib64/",
            "su - root -c cp   $folder/gnupg/libbz2.so.1.0 /system/lib64/",
            "su - root -c cp   $folder/gnupg/libcrypt.so /system/lib64/",
            "su - root -c cp   $folder/gnupg/libcrypto.so.3 /system/lib64/",
            "su - root -c cp   $folder/gnupg/libgcrypt.so /system/lib64/",
            "su - root -c cp   $folder/gnupg/libgpg-error.so /system/lib64/",
            "su - root -c cp   $folder/gnupg/libncursesw.so.6 /system/lib64/",
            "su - root -c cp   $folder/gnupg/libnpth.so /system/lib64/",
            "su - root -c cp   $folder/gnupg/libreadline.so.8 /system/lib64/",
            "su - root -c cp   $folder/gnupg/libsqlite3.so /system/lib64/",
            "su - root -c cp   $folder/gnupg/libsqlite3.so.0 /system/lib64/",
            "su - root -c chmod -R 0755 /system/lib64/libpcre2-8.so",
            "su - root -c chmod -R 0755 /system/lib64/libz.so.1",
            "su - root -c chmod -R 0755 /system/lib64/libiconv.so",
            "su - root -c chmod -R 0755 /system/lib64/libcrypto.so.3",
            "su - root -c chmod -R 0755 /system/lib64/libandroid-support.so",
            "su - root -c chmod -R 0755 /system/lib64/libassuan.so",
            "su - root -c chmod -R 0755 /system/lib64/libbz2.so.1.0",
            "su - root -c chmod -R 0755 /system/lib64/libcrypt.so",
            "su - root -c chmod -R 0755 /system/lib64/libcrypto.so.3",
            "su - root -c chmod -R 0755 /system/lib64/libgpg-error.so",
            "su - root -c chmod -R 0755 /system/lib64/libncursesw.so.6",
            "su - root -c chmod -R 0755 /system/lib64/libnpth.so",
            "su - root -c chmod -R 0755 /system/lib64/libreadline.so.8",
            "su - root -c chmod -R 0755 /system/lib64/libsqlite3.so",
            "su - root -c chmod  0755 /system/lib64/libsqlite3.so.0",
            "su - root -c chmod -R  0755 /system/lib64/",
            "su - root -c chmod +x  /system/bin/gpg",
            "su - root -c chmod -R  0755 /system/bin/gpg",
        )

        var process: Process?

        for (command in commands) {
            process = Runtime.getRuntime().exec(command)
            process.waitFor() // Wait for the command to finish
            if (process.exitValue() != 0) {
                Toast.makeText(context, "Ошибка при копирование GnuPG: $command", Toast.LENGTH_LONG)
                    .show()
                return
            }
        }
        Toast.makeText(context, "Копирование  GnuPG завершенo", Toast.LENGTH_SHORT).show()
    }

    // Вспомогательная функция для выполнения shell-команд
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
}