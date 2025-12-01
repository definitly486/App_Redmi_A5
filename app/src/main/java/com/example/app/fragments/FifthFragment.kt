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
import com.example.app.decryptWithOpenSslFormat2
import com.example.app.download
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FifthFragment : Fragment() {

    private lateinit var downloadPlumaProfileButton: View
    private lateinit var installPlumaProfileButton: View
    private lateinit var downloadTelegramProfileButton: View
    private lateinit var installTelegramProfileButton: View

    private lateinit var downloadauthProfileButton: View
    private lateinit var installauthProfileButton: View

    private lateinit var downloadk9mailProfileButton : View
    private lateinit var installk9mailProfileButton: View
    private lateinit var editTextPassword: EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_fifth, container, false)

        editTextPassword = view.findViewById(R.id.editTextPassword)
        downloadPlumaProfileButton = view.findViewById(R.id.downloadplumaprofile)
        installPlumaProfileButton = view.findViewById(R.id.installplumaprofile)
        downloadTelegramProfileButton = view.findViewById(R.id.downloadtelegramprofile)
        installTelegramProfileButton = view.findViewById(R.id.installtelegramprofile)

        downloadk9mailProfileButton = view.findViewById(R.id.downloadk9mailprofile)
        installk9mailProfileButton = view.findViewById(R.id.installk9mailprofile)

        downloadauthProfileButton = view.findViewById(R.id.downloadgoogleauth)
        installauthProfileButton = view.findViewById(R.id.installgoogleauth)


        downloadPlumaProfileButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                downloadProfile()
            }
        }

        installPlumaProfileButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                installProfile()
            }
        }

        downloadTelegramProfileButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                downloadTelegramProfile()
            }
        }

        installTelegramProfileButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                installTelegramProfile()
            }
        }


        downloadk9mailProfileButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                downloadk9mailProfile()
            }
        }

        installk9mailProfileButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                installk9mailProfile()
            }
        }


        downloadauthProfileButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                downloadauthProfile()
            }
        }

        installauthProfileButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                installauthProfile()
            }
        }


        return view
    }

    private suspend fun installTelegramProfile() {

        // Проверка root-доступа устройства
        if (RootChecker.hasRootAccess(requireContext())) {
            Toast.makeText(requireContext(), "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(requireContext(), "Root-доступ отсутствует.Профиль не будет установлен.", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка возможности записи в папку '/system'
        val pathToCheck = "/system"
        if (RootChecker.checkWriteAccess(pathToCheck)) {
            Toast.makeText(
                requireContext(),
                "Запись в '$pathToCheck' возможна!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Запись в '$pathToCheck' невозможна.Профиль не будет установлен",
                Toast.LENGTH_SHORT
            ).show()
            return
        }



        withContext(Dispatchers.IO) {
            try {
                // Получаем введенный пароль из поля ввода
                val enteredPassword = editTextPassword.text.toString()

                // Проверка на пустой пароль
                if (enteredPassword.isEmpty()) {
                    showToast("Пароль не введен. Пожалуйста, введите пароль.")
                    return@withContext
                }

                // Преобразование пароля и установка
                decryptWithOpenSslFormat2(requireContext(),"org.thunderdog.challegram" ,enteredPassword)
                showToast("Архив успешно установлен и извлечён!")
            } catch (e: Exception) {
                showToast("Ошибка при установке и извлечении архива: ${e.message}")
            }
        }
    }

    private fun downloadTelegramProfile() {
        download(requireContext(), "https://github.com/definitly486/redmia5/releases/download/shared/org.thunderdog.challegram.tar.enc")
    }

    private fun downloadProfile() {
        download(requireContext(), "https://github.com/definitly486/redmia5/releases/download/shared/com.qflair.browserq.tar.enc")
    }


    private fun downloadk9mailProfile() {
        download(requireContext(), "https://github.com/definitly486/redmia5/releases/download/shared/com.fsck.k9.tar.enc")
    }


    private fun downloadauthProfile() {
        download(requireContext(), "https://github.com/definitly486/redmia5/releases/download/shared/com.google.android.apps.authenticator2.tar.enc")
    }


    private suspend fun installProfile() {

        // Проверка root-доступа устройства
        if (RootChecker.hasRootAccess(requireContext())) {
            Toast.makeText(requireContext(), "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(requireContext(), "Root-доступ отсутствует.Профиль не будет установлен.", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка возможности записи в папку '/system'
        val pathToCheck = "/system"
        if (RootChecker.checkWriteAccess(pathToCheck)) {
            Toast.makeText(
                requireContext(),
                "Запись в '$pathToCheck' возможна!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Запись в '$pathToCheck' невозможна.Профиль не будет установлен",
                Toast.LENGTH_SHORT
            ).show()
            return
        }



        withContext(Dispatchers.IO) {
            try {
                // Получаем введенный пароль из поля ввода
                val enteredPassword = editTextPassword.text.toString()

                // Проверка на пустой пароль
                if (enteredPassword.isEmpty()) {
                    showToast("Пароль не введен. Пожалуйста, введите пароль.")
                    return@withContext
                }

                // Преобразование пароля и установка
                decryptWithOpenSslFormat2(requireContext(),"com.qflair.browserq" ,enteredPassword)
                showToast("Архив успешно установлен и извлечён!")
            } catch (e: Exception) {
                showToast("Ошибка при установке и извлечении архива: ${e.message}")
            }
        }
    }




    private suspend fun installauthProfile() {


        // Проверка root-доступа устройства
        if (RootChecker.hasRootAccess(requireContext())) {
            Toast.makeText(requireContext(), "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(requireContext(), "Root-доступ отсутствует.Профиль не будет установлен.", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка возможности записи в папку '/system'
        val pathToCheck = "/system"
        if (RootChecker.checkWriteAccess(pathToCheck)) {
            Toast.makeText(
                requireContext(),
                "Запись в '$pathToCheck' возможна!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Запись в '$pathToCheck' невозможна.Профиль не будет установлен",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        withContext(Dispatchers.IO) {
            try {
                // Получаем введенный пароль из поля ввода
                val enteredPassword = editTextPassword.text.toString()

                // Проверка на пустой пароль
                if (enteredPassword.isEmpty()) {
                    showToast("Пароль не введен. Пожалуйста, введите пароль.")
                    return@withContext
                }

                // Преобразование пароля и установка
                decryptWithOpenSslFormat2(requireContext(),"com.google.android.apps.authenticator2" ,enteredPassword)
                showToast("Архив успешно установлен и извлечён!")
            } catch (e: Exception) {
                showToast("Ошибка при установке и извлечении архива: ${e.message}")
            }
        }
    }


    private suspend fun installk9mailProfile() {

        // Проверка root-доступа устройства
        if (RootChecker.hasRootAccess(requireContext())) {
            Toast.makeText(requireContext(), "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(requireContext(), "Root-доступ отсутствует.Профиль не будет установлен.", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка возможности записи в папку '/system'
        val pathToCheck = "/system"
        if (RootChecker.checkWriteAccess(pathToCheck)) {
            Toast.makeText(
                requireContext(),
                "Запись в '$pathToCheck' возможна!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Запись в '$pathToCheck' невозможна.Профиль не будет установлен",
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        withContext(Dispatchers.IO) {
            try {
                // Получаем введенный пароль из поля ввода
                val enteredPassword = editTextPassword.text.toString()

                // Проверка на пустой пароль
                if (enteredPassword.isEmpty()) {
                    showToast("Пароль не введен. Пожалуйста, введите пароль.")
                    return@withContext
                }

                // Преобразование пароля и установка
                decryptWithOpenSslFormat2(requireContext(),"com.fsck.k9" ,enteredPassword)
                showToast("Архив успешно установлен и извлечён!")
            } catch (e: Exception) {
                showToast("Ошибка при установке и извлечении архива: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }
}