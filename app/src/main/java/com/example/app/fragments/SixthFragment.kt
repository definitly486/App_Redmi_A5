@file:Suppress("SpellCheckingInspection")

package com.example.app.fragments
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SixthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sixth, container, false)

        // Ищем элемент UI, где вводится ссылка на репозиторий
        val repoUrlField = view.findViewById<EditText>(R.id.repo_url_field)
        // Ищем кнопку для начала процесса клонирования
        val buttonClone = view.findViewById<Button>(R.id.button_clone)

        // Назначаем слушатель кликов на кнопке
        buttonClone.setOnClickListener {
            cloneGIT(repoUrlField.text.toString())
        }

        return view
    }

    // Корутинный метод для клонирования репозитория
    private fun cloneGIT(repoUrl: String) {


        CoroutineScope(Dispatchers.Main).launch {
            val gitCloneInstance = GitClone2(null)

            // Установка адреса удалённого репозитория
            gitCloneInstance.setRepositoryUrl(repoUrl)

            // Определение родительского каталога для скачивания
            val parentDirectory =getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            // Извлечение названия репозитория из URL
            val repoName = extractRepoNameFromURL(repoUrl)

            // Создание полного пути для клонирования (подкаталог с названием репозитория)
            val localPath = "$parentDirectory/$repoName"
            gitCloneInstance.setLocalPath(localPath)

            // Клонируем репозиторий асинхронно
            val result = withContext(Dispatchers.IO) {
                gitCloneInstance.cloneRepository().isSuccess
            }

            if (result) {
                Toast.makeText(requireContext(), "Репозиторий '$repoName' успешно клонирован.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Ошибка клонирования репозитория.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Вспомогательная функция для извлечения имени репозитория из URL
    fun extractRepoNameFromURL(url: String): String {
        return url.substringBeforeLast(".git").substringAfterLast("/")
    }




}