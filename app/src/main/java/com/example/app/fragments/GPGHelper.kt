@file:Suppress("SpellCheckingInspection")

package com.example.app.fragments

import java.io.BufferedReader
import java.io.InputStreamReader
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.*
import org.bouncycastle.openpgp.jcajce.*
import org.bouncycastle.openpgp.operator.jcajce.JcePBEDataDecryptorFactoryBuilder
import java.io.File
import java.io.FileInputStream
import java.security.Security
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder

class GPGHelper(private val gpgExecutable: String = "/system/bin/gpg") {

    /**
     * Декриптовывает файл с использованием указанной команды gpg.
     *
     * @param inputFile путь к зашифрованному файлу
     * @param outputFile путь к расшифрованному файлу
     * @param passphrase пароль для расшифровки
     * @return true, если команда выполнена успешно, иначе false
     */

    //функция расшифровки с использованием внешней программы gnupg
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

//функция расшифровки с использованием библиотек
fun decryptGpgSymmetric(
    inputFilePath: String,
    outputFilePath: String,
    passphrase: String
): Boolean {
    return try {
        decryptGpgSymmetricInternal(
            inputFile = File(inputFilePath),
            outputFile = File(outputFilePath),
            passphrase = passphrase.toCharArray()
        )
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}


    private fun decryptGpgSymmetricInternal(
        inputFile: File,
        outputFile: File,
        passphrase: CharArray
    ) {
        Security.addProvider(BouncyCastleProvider())

        val decoder = PGPUtil.getDecoderStream(FileInputStream(inputFile))
        val pgpFactory = JcaPGPObjectFactory(decoder)

        var obj = pgpFactory.nextObject()

        val encryptedDataList = when (obj) {
            is PGPEncryptedDataList -> obj
            is PGPMarker -> pgpFactory.nextObject() as PGPEncryptedDataList
            else -> obj as PGPEncryptedDataList
        }

        val encryptedData = encryptedDataList.encryptedDataObjects.next() as PGPPBEEncryptedData

        val decryptorFactory = JcePBEDataDecryptorFactoryBuilder(
            JcaPGPDigestCalculatorProviderBuilder().build()
        )
            .setProvider("BC")
            .build(passphrase)

        val clear = encryptedData.getDataStream(decryptorFactory)
        val plainFactory = JcaPGPObjectFactory(clear)
        val message = plainFactory.nextObject()

        val literalData = when (message) {
            is PGPLiteralData -> message
            is PGPCompressedData -> {
                val compressedFactory = JcaPGPObjectFactory(message.dataStream)
                compressedFactory.nextObject() as PGPLiteralData
            }
            else -> throw IllegalArgumentException("Неизвестный формат PGP данных")
        }

        literalData.inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }


}