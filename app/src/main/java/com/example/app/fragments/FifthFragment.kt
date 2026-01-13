@file:Suppress("SpellCheckingInspection")

package com.example.app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.R
import com.example.app.download // ваша функция скачивания
import com.example.app.decryptWithOpenSslFormat2 // extension на CoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FifthFragment : Fragment() {

    private lateinit var editTextPassword: EditText

    // Кнопки скачивания
    private lateinit var downloadPluma: View
    private lateinit var downloadTelegram: View
    private lateinit var downloadK9Mail: View
    private lateinit var downloadAuth: View
    private lateinit var downloadMetamask: View
    private lateinit var downloadMax: View
    private lateinit var downloadMynalog: View

    // Кнопки установки
    private lateinit var installPluma: View
    private lateinit var installTelegram: View
    private lateinit var installK9Mail: View
    private lateinit var installAuth: View
    private lateinit var installMetamask: View
    private lateinit var installMax: View
    private lateinit var installMynalog: View

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_fifth, container, false)

        editTextPassword = view.findViewById(R.id.editTextPassword)

        // Скачивание
        downloadPluma = view.findViewById(R.id.downloadplumaprofile)
        downloadTelegram = view.findViewById(R.id.downloadtelegramprofile)
        downloadK9Mail = view.findViewById(R.id.downloadk9mailprofile)
        downloadAuth = view.findViewById(R.id.downloadgoogleauth)
        downloadMetamask = view.findViewById(R.id.downloadmetamaskprofile)
        downloadMax = view.findViewById(R.id.downloadmaxrofile)
        downloadMynalog = view.findViewById(R.id.downloadmynalogrofile)

        // Установка
        installPluma = view.findViewById(R.id.installplumaprofile)
        installTelegram = view.findViewById(R.id.installtelegramprofile)
        installK9Mail = view.findViewById(R.id.installk9mailprofile)
        installAuth = view.findViewById(R.id.installgoogleauth)
        installMetamask = view.findViewById(R.id.installmetamskprofile) // Исправлена опечатка!
        installMax = view.findViewById(R.id.installmaxprofile)
        installMynalog = view.findViewById(R.id.installmynalogprofile)

        // Обработчики скачивания
        downloadPluma.setOnClickListener { downloadProfile("com.qflair.browserq.tar.enc") }
        downloadTelegram.setOnClickListener { downloadProfile("org.thunderdog.challegram.tar.enc") }
        downloadK9Mail.setOnClickListener { downloadProfile("com.fsck.k9.tar.enc") }
        downloadAuth.setOnClickListener { downloadProfile("com.google.android.apps.authenticator2.tar.enc") }
        downloadMetamask.setOnClickListener { downloadProfile("io.metamask.tar.enc") }
        downloadMax.setOnClickListener { downloadProfile("ru.oneme.app.tar.enc") }
        downloadMynalog.setOnClickListener { downloadProfile("com.gnivts.selfemployed.tar.enc") }
        // Обработчики установки
        installPluma.setOnClickListener { launchInstall("com.qflair.browserq") }
        installTelegram.setOnClickListener { launchInstall("org.thunderdog.challegram") }
        installK9Mail.setOnClickListener { launchInstall("com.fsck.k9") }
        installAuth.setOnClickListener { launchInstall("com.google.android.apps.authenticator2") }
        installMetamask.setOnClickListener { launchInstall("io.metamask") }
        installMax.setOnClickListener { launchInstall("ru.oneme.app") }
        installMynalog.setOnClickListener { launchInstall("com.gnivts.selfemployed") }
        return view
    }

    private fun downloadProfile(fileName: String) {
        val url = "https://github.com/definitly486/redmia5/releases/download/shared/$fileName"
        download(requireContext(), url)
        Toast.makeText(requireContext(), "Скачивание $fileName начато...", Toast.LENGTH_SHORT).show()
    }

    private fun launchInstall(archiveName: String) {
        CoroutineScope(Dispatchers.Main).launch {
            installProfile(archiveName)
        }
    }

    private suspend fun installProfile(archiveName: String) {
        val context = requireContext()

        // 1. Проверка root и записи в /system
        if (!RootChecker.hasRootAccess(context)) {
            showToast("Root-доступ отсутствует. Профиль не будет установлен.")
            return
        }

        if (!RootChecker.checkWriteAccess("/system")) {
            showToast("Нет прав записи в /system. Профиль не будет установлен.")
            return
        }

        // 2. Проверка наличия зашифрованного файла
        val folder = context.getExternalFilesDir("shared") ?: run {
            showToast("Не удалось получить папку хранения.")
            return
        }

        val encryptedFile = File(folder, "$archiveName.tar.enc")
        if (!encryptedFile.exists()) {
            showToast("Зашифрованный файл не найден: ${encryptedFile.name}")
            return
        }

        // 3. Получение пароля
        val password = editTextPassword.text.toString().trim()
        if (password.isEmpty()) {
            showToast("Пароль не введён. Пожалуйста, введите пароль.")
            return
        }

        // 4. Расшифровка и распаковка (в IO-потоке)
        withContext(Dispatchers.IO) {
            try {
                // Важно: decryptWithOpenSslFormat2 — extension на CoroutineScope
                CoroutineScope(Dispatchers.IO).decryptWithOpenSslFormat2(context, archiveName, password)

                withContext(Dispatchers.Main) {
                    showToast("Профиль $archiveName успешно установлен!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToast("Ошибка установки: ${e.message ?: "Неизвестная ошибка"}")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}