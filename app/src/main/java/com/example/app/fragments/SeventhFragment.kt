@file:Suppress("SpellCheckingInspection")

package com.example.app.fragments

import DownloadHelper
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.R
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
class SeventhFragment  : Fragment()  {

    private lateinit var downloadHelper: DownloadHelper

    fun getDownloadFolder(): File? {
        return context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    }

    fun getDownloadFolder2(): File? {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (dir?.exists() == false) dir.mkdirs()
        return dir
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_seventh, container, false)

        // Инициализация DownloadHelper
        downloadHelper = DownloadHelper(requireContext())

        // Настройка кнопок
        setupButtons(view)
        return view

    }

    private fun setupButtons(view: View) {

        // Кнопка скачивания gate
        val downloadgate = view.findViewById<Button>(R.id.download_gate)
        downloadgate.setOnClickListener { downloadGATE() }

        // Кнопка установки gate
        val installgate = view.findViewById<Button>(R.id.installgate)
        installgate.setOnClickListener { installGATE() }

        // Кнопка установки binance
        val installbinance = view.findViewById<Button>(R.id.installbinance)
        installbinance.setOnClickListener { installBINANCE() }
    }

    private fun downloadGATE(){
        downloadHelper.downloadTool("https://github.com/definitly486/redmia5/releases/download/apk/gate.base.zip","gate") { file ->
            handleDownloadResult(file, "gate")
        }
    }

    private fun installGATE(){
        unzipgate("gate.base.zip")
        val publicDownloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        downloadHelper.installGate( "gate.apk")
    }

    private fun installBINANCE() {
        val folder = getDownloadFolder2() ?: return
        val apkFile = File(folder, "com.binance.dev-100300004.xapk")

        // Начинаем скачивание
        downloadHelper.downloadToPublic("https://github.com/definitly486/redmia5/releases/download/apk/com.binance.dev-100300004.xapk")
        fixApkPermissions(apkFile.absolutePath)
    }

    // Перегружаем функцию, чтобы принимать как String, так и File
    private fun fixApkPermissions(file: File) {
        fixApkPermissions(file.absolutePath)
    }

    private fun fixApkPermissions(filePath: String) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val writer = DataOutputStream(process.outputStream)

            writer.writeBytes("chown 1000:1000 \"$filePath\"\n")
            writer.writeBytes("chmod 644 \"$filePath\"\n")
            writer.writeBytes("exit\n")
            writer.flush()
            writer.close()

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Toast.makeText(requireContext(), "Права успешно исправлены (644, system:system)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Root выполнен, но команда завершилась с ошибкой", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Нет root-доступа или ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }



    private fun handleDownloadResult(file: File?, @Suppress("SameParameterValue") name: String) {
        if (file != null) {
            Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Ошибка загрузки файла $name", Toast.LENGTH_SHORT).show()
        }
    }

    fun unzipgate(filename: String): Boolean {
        val folder = getDownloadFolder() ?: return false
        val zipFile = File(folder, filename)


        // Check if target APK already exists
        val targetApk = File(folder, "gate.apk")
        if (targetApk.exists()) {
            Toast.makeText(context, "Файл gate.apk  уже существует", Toast.LENGTH_SHORT).show()

            return true
        }

        try {
            FileInputStream(zipFile).use { fis ->
                ZipInputStream(fis).use { zis ->
                    var entry: ZipEntry?
                    while (zis.nextEntry.also { entry = it } != null) {
                        val destFile = File(folder, entry!!.name)
                        destFile.parentFile?.mkdirs()
                        if (!entry.isDirectory) {
                            FileOutputStream(destFile).use { fos ->
                                val buffer = ByteArray(4096)
                                var count: Int
                                while (zis.read(buffer).also { count = it } != -1) {
                                    fos.write(buffer, 0, count)
                                }
                            }
                        }
                        zis.closeEntry()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

}

