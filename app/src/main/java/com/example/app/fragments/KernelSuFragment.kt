@file:Suppress("SpellCheckingInspection")

package com.example.app.fragments

import DownloadHelper
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.app.KernelSUInstaller
import com.example.app.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KernelSuFragment : Fragment() {

    private lateinit var downloadHelper: DownloadHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        downloadHelper = DownloadHelper(requireContext())
        val view = inflater.inflate(R.layout.fragment_kernelsu, container, false)
        setupButtons(view)
        return view
    }

    private fun setupButtons(view: View) {
        val installButton = view.findViewById<Button>(R.id.install_apatch_ksu_zip)

        installButton.setOnClickListener {
            val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            Log.d("KernelInstaller", "[$time] Нажата кнопка установки APatch-KSU.zip")

            Toast.makeText(requireContext(), "Установка APatch-KSU…", Toast.LENGTH_LONG).show()

            Thread {
                val success = KernelSUInstaller.installAPatchKSU()

                activity?.runOnUiThread {
                    if (success) {
                        // Показываем диалог с предложением перезагрузки
                        AlertDialog.Builder(requireContext())
                            .setTitle("Установка завершена")
                            .setMessage("APatch-KSU успешно установлен!\n\nПерезагрузить устройство сейчас?")
                            .setPositiveButton("Перезагрузить") { _, _ ->
                                try {
                                    Runtime.getRuntime().exec("su -mm -c reboot")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        requireContext(),
                                        "Не удалось выполнить перезагрузку",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .setNegativeButton("Позже", null)
                            .setCancelable(false)
                            .show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка: APatch-KSU.zip не найден в папке Download\nили установка провалилась",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.start()
        }

        // Кнопка скачивания APatch-KSU.zip
        view.findViewById<Button>(R.id.downloadksuzip).setOnClickListener {
            downloadHelper.downloadToPublic(
                "https://github.com/definitly486/redmia5/releases/download/root/APatch-KSU.zip"
            )
            Toast.makeText(requireContext(), "Скачивание APatch-KSU.zip начато…", Toast.LENGTH_SHORT).show()
        }

        //Кнопка распаковки и установки KernelSU
        view.findViewById<Button>(R.id.install_kermelsu).setOnClickListener {
            installKernelSuManager(requireContext())
        }
    }
    // Функция распаковки apk
    private val TAG = "KernelSU_Installer"

    private fun installKernelSuManager(context: Context) {
        val apkFile = extractAndGetApkFile(context) ?: run {
            Log.e(TAG, "APK не распакован")
            return
        }

        // ВНИМАНИЕ: именно .fileprovider — как у тебя в манифесте!
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",  // ← ТОЧНО как в манифесте!
            apkFile
        )

        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // Эти экстра работают на Android 14–15
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.packageName)
        }

        try {
            context.startActivity(intent)
            Log.i(TAG, "Системный установщик открыт (Android 15 — через .fileprovider)")
        } catch (e: Exception) {
            Log.e(TAG, "Не удалось открыть установщик APK", e)
            Toast.makeText(context, "Не удалось установить. Проверьте разрешения на установку из этого приложения.", Toast.LENGTH_LONG).show()
        }
    }
    private fun extractAndGetApkFile(context: Context): File? {
        val apkName = "KernelSU_v1.0.5_12081-release.apk"
        val targetFile = File(context.cacheDir, apkName)

        if (targetFile.exists() && targetFile.length() > 2_000_000) {
            Log.i(TAG, "APK уже в кэше: ${targetFile.absolutePath} (${targetFile.length()/1024/1024} МБ)")
            return targetFile
        }

        if (targetFile.exists()) targetFile.delete()

        return try {
            context.assets.open(apkName).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.i(TAG, "APK успешно распакован в кэш")
            targetFile
        } catch (e: Exception) {
            Log.e(TAG, "Не удалось распаковать APK из assets", e)
            targetFile.delete()
            null
        }
    }
}