@file:Suppress("SpellCheckingInspection")

package com.example.app.fragments

import java.io.BufferedReader
import java.io.InputStreamReader

class GPGHelper(private val gpgExecutable: String = "/system/bin/gpg") {

    /**
     * Декриптовывает файл с использованием указанной команды gpg.
     *
     * @param inputFile путь к зашифрованному файлу
     * @param outputFile путь к расшифрованному файлу
     * @param passphrase пароль для расшифровки
     * @return true, если команда выполнена успешно, иначе false
     */
    fun decryptFile(inputFile: String, outputFile: String, passphrase: String): Boolean {
        // Формируем команду с указанием переменной окружения HOME
        val command = arrayOf("sh", "-c", "HOME='/storage/emulated/0/Download'; $gpgExecutable --output '$outputFile' --batch --passphrase '${passphrase}' -d '$inputFile'")

        // Запускаем процесс
        val process = ProcessBuilder(*command)
            .redirectErrorStream(true) // Объединяем потоки stdout и stderr
            .start()

        // Ждём завершения процесса
        val exitCode = process.waitFor()

        if (exitCode == 0) {
            printResult(process)
            return true
        } else {
            printError(process)
            return false
        }
    }
    private fun printResult(process: Process) {
        BufferedReader(InputStreamReader(process.inputStream)).lines().forEach(::println)
    }

    private fun printError(process: Process) {
        BufferedReader(InputStreamReader(process.errorStream)).lines().forEach(::println)
    }
}