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
import java.io.File

class PythonFragment : Fragment() {

    private lateinit var downloadHelper: DownloadHelper
    private lateinit var downloadHelper2: DownloadHelper2
    private var downloadFolder: File? = null
    fun getDownloadFolder(): File? {
        return context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_python, container, false)
        // Инициализация DownloadHelper
        downloadHelper = DownloadHelper(requireContext())
        downloadHelper2 = DownloadHelper2(requireContext())
        downloadFolder = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        setupButtons(view)
        return view
    }

    private fun setupButtons(view: View) {
        //Кнопка установки python3
        val installPython = view.findViewById<Button>(R.id.installpython3)
        installPython.setOnClickListener { installPYTHON3() }

        val installEnvPython = view.findViewById<Button>(R.id.installenvpython3)
        installEnvPython.setOnClickListener { installENVPYTHON3() }

        val installPIPPython = view.findViewById<Button>(R.id.installpippython3)
        installPIPPython.setOnClickListener { installPIPPYTHON3() }

        val installytDLP = view.findViewById<Button>(R.id.installytdlp)
        installytDLP.setOnClickListener { installYTDLP() }

    }

    private fun installYTDLP() {

        val pathToCheck = "/data/local/tmp/env/lib/python3.13/site-packages//yt_dlp-2025.11.12.dist-info"
        if (checkDirectoryExists(pathToCheck)) {
            Toast.makeText(context, "Директория ${pathToCheck} существует", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Директория ${pathToCheck} не существует", Toast.LENGTH_SHORT).show()

            val commands = arrayOf(
                "su - root -c  pip  install yt-dlp",
                )

            var process: Process?

            for (command in commands) {
                process = Runtime.getRuntime().exec(command)
                process.waitFor() // Wait for the command to finish
                if (process.exitValue() != 0) {
                    Toast.makeText(context, "Ошибка при установке yt-dlp: $command", Toast.LENGTH_LONG)
                        .show()
                    return
                }
            }
            Toast.makeText(context, "Устанвка yt-dlp завершенo", Toast.LENGTH_SHORT).show()


        }



        val pipScript = """
    #!/system/bin/sh
if [ "$(id -u)" -ne 0 ]; then
    echo "Ошибка: этот скрипт должен запускаться от root" >&2
    exit 1
fi
    source /data/local/tmp/env/bin/activate
    yt-dlp "$@"
""".trimIndent()

        try {
            val file = File("$downloadFolder/yt-dlp")
            file.writeText(pipScript)
            file.setExecutable(true)  // chmod +x
            Toast.makeText(context, "yt-dlp успешно создан!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }


        Toast.makeText(context, "Установка yt-dlp ...", Toast.LENGTH_SHORT).show()
        val commands = arrayOf(
            "su - root -c mount -o rw,remount /system",
            "su - root -c cp $downloadFolder/yt-dlp /system/bin",
            "su - root -c chmod +x /system/bin/yt-dlp",
            "su - root -c chmod 0755 /system/bin/yt-dlp"

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
        Toast.makeText(context, "Создание yt-dlp  завершенo", Toast.LENGTH_SHORT).show()
    }

    fun checkFileExists(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists()
    }

    // Проверяем существование директории
    fun checkDirectoryExists(directoryPath: String): Boolean {
        val directory = File(directoryPath)
        return directory.exists() && directory.isDirectory
    }

    private fun  installPIPPYTHON3() {
        downloadHelper2.installpippython3()
    }


   private  fun  installENVPYTHON3() {
       if (isPythonFolderExists()) {
           downloadHelper2.installenvpython3()
       } else {
           Toast.makeText(requireContext(), "Python3 не установлен", Toast.LENGTH_SHORT).show()
       }
    }

    fun isPythonFolderExists(): Boolean {
        val path = "/data/local/tmp/python-android-aarch64"
        val folder = File(path)
        return folder.exists() && folder.isDirectory
    }

    private fun installPYTHON3() {
        val folder = getDownloadFolder() ?: return
        val tarGzFile = File(folder, "python-3.13-android-aarch64.tar.gz")
        val outputDir = File(folder, "")
        if (!tarGzFile.exists()) {
            Toast.makeText(requireContext(), "Файл python-3.13-android-aarch64.tar.gz не существует", Toast.LENGTH_SHORT).show()
            downloadHelper.downloadFileSimple("https://github.com/definitly486/redmia5/releases/download/python3/python-3.13-android-aarch64.tar.gz")
            return
        }
        downloadHelper2 = DownloadHelper2(requireContext())
        downloadHelper2.decompressTarGz(tarGzFile, outputDir)
        Thread.sleep(3000L)
        downloadHelper2.copypython3()
    }



}