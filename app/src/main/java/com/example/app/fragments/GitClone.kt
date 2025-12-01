@file:Suppress("SpellCheckingInspection")

package com.example.app.fragments

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import java.io.File

class GitClone {

    /**
     * Функция для клонирования репозитория Git
     *
     * @return Результат выполнения операции клонирования (true - успех, исключение - ошибка)
     */
    suspend fun cloneRepository(): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            val remoteUri = "https://github.com/definitly486/DCIM"
            val localDirectory = File("/storage/emulated/0/Android/data/com.example.app/files/Download/DCIM")

            // Создание директории, если она не существует
            if (!localDirectory.exists()) {
                localDirectory.mkdirs()
            }

            // Выполняем операцию клонирования
            Git.cloneRepository()
                .setURI(remoteUri)
                .setDirectory(localDirectory)
                .call()

            Result.success(true) // Возвращаем успешный результат
        } catch (e: Exception) {
            println("Ошибка клонирования репозитория: ${e.message}")
            Result.failure(e) // Возвращаем результат с ошибкой
        }
    }
}

class GitClone2(private var repositoryUrl: String?) {
    private lateinit var localDirectory: File

    /**
     * Устанавливает адрес удалённого репозитория
     *
     * @param url Адрес репозитория
     */
    fun setRepositoryUrl(url: String) {
        this.repositoryUrl = url
    }

    /**
     * Устанавливает локальный путь для клонируемых файлов
     *
     * @param path Локальная папка назначения
     */
    fun setLocalPath(path: String) {
        this.localDirectory = File(path)
    }

    /**
     * Клонирует указанный репозиторий
     *
     * @return true в случае успешного завершения, иначе ложь
     */
    suspend fun cloneRepository(): Result<Boolean> = withContext(Dispatchers.IO) {
        requireNotNull(repositoryUrl) { "Адрес репозитория не указан." }
        require(::localDirectory.isInitialized) { "Локальный путь не установлен." }

        try {
            // Проверяем наличие локальной директории и создаем её, если отсутствует
            if (!localDirectory.exists()) {
                localDirectory.mkdirs()
            }

            // Осуществляем клонирование
            Git.cloneRepository()
                .setURI(repositoryUrl!!)
                .setDirectory(localDirectory)
                .call()

            Result.success(true) // Успех!
        } catch (e: Exception) {
            println("Ошибка клонирования репозитория: ${e.message}")
            Result.failure(e) // Ошибка
        }
    }
}