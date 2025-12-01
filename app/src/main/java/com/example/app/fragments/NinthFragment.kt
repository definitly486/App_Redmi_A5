@file:Suppress("SpellCheckingInspection")

package com.example.app.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app.R
import kotlinx.coroutines.*
import java.io.*
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import android.content.ContentValues
import android.provider.MediaStore

class NinthFragment : Fragment() {

    private lateinit var tvSelectedFile: TextView
    private lateinit var btnSelectFile: Button
    private lateinit var etPassword: EditText
    private lateinit var btnDecrypt: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView

    private var selectedFileUri: Uri? = null
    private var finalOutputUri: Uri? = null // Ссылка на файл в Downloads

    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { handleFileSelected(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_ninth, container, false)
        initViews(rootView)
        setupUI()
        return rootView
    }
    private fun initViews(view: View) {
        tvSelectedFile = view.findViewById(R.id.tvSelectedFile)
        btnSelectFile = view.findViewById(R.id.btnSelectFile)
        etPassword = view.findViewById(R.id.etPassword)
        btnDecrypt = view.findViewById(R.id.btnDecrypt)
        progressBar = view.findViewById(R.id.progressBar)
        tvStatus = view.findViewById(R.id.tvStatus)
    }

    private fun setupUI() {
        btnSelectFile.setOnClickListener { selectFile() }
        btnDecrypt.setOnClickListener { decryptFile() }
        etPassword.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateDecryptButtonState()
            }
        })
    }

    private fun selectFile() {
        selectFileLauncher.launch(arrayOf("*/*"))
    }

    private fun handleFileSelected(uri: Uri) {
        selectedFileUri = uri
        try {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (_: Exception) { /* игнорируем, если временный доступ */ }

        tvSelectedFile.text = "Выбран: ${getFileName(uri)}"
        updateDecryptButtonState()
    }

    private fun getFileName(uri: Uri): String {
        return try {
            requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else "файл"
            } ?: "файл"
        } catch (_: Exception) {
            uri.lastPathSegment ?: "файл"
        }
    }

    private fun updateDecryptButtonState() {
        btnDecrypt.isEnabled = selectedFileUri != null && etPassword.text.toString().trim().isNotEmpty()
    }

    private fun decryptFile() {
        val password = etPassword.text.toString().trim()
        if (password.isEmpty() || selectedFileUri == null) return

        progressBar.visibility = View.VISIBLE
        tvStatus.text = "Расшифровка..."
        btnDecrypt.isEnabled = false

        val tempFile = getTempOutputFile()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                decryptWithOpenSslFormat(selectedFileUri!!, tempFile, password)
                val downloadedUri = saveFileToDownloads(tempFile, "decrypted_${System.currentTimeMillis()}.txt")
                finalOutputUri = downloadedUri

                withContext(Dispatchers.Main) {
                    onSuccess()
                    tempFile.delete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                    tempFile.delete()
                }
            }
        }
    }

    private fun getTempOutputFile(): File {
        val cacheDir = requireContext().cacheDir
        return File(cacheDir, "temp_decrypted_${System.currentTimeMillis()}.bin")
    }

    private fun decryptWithOpenSslFormat(inputUri: Uri, outputFile: File, password: String) {
        requireContext().contentResolver.openInputStream(inputUri)?.use { input ->
            FileOutputStream(outputFile).use { fileOut ->

                // 1. Читаем "Salted__"
                val header = ByteArray(8)
                if (input.read(header) != 8 || !header.contentEquals("Salted__".toByteArray())) {
                    throw IllegalArgumentException("Файл не в формате OpenSSL (нет 'Salted__')")
                }

                // 2. Читаем соль (8 байт)
                val salt = ByteArray(8)
                if (input.read(salt) != 8) throw IOException("Не удалось прочитать соль")

                // 3. Генерируем ключ (32) + IV (16) через PBKDF2-HMAC-SHA256
                val (key, iv) = deriveKeyAndIvWithPbkdf2(password, salt)

                // 4. Расшифровка
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
                    init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
                }

                CipherOutputStream(fileOut, cipher).use { cipherOut ->
                    input.copyTo(cipherOut)
                }
            }
        } ?: throw IOException("Не удалось открыть зашифрованный файл")
    }

    private fun deriveKeyAndIvWithPbkdf2(password: String, salt: ByteArray): Pair<ByteArray, ByteArray> {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, 100000, 384) // 384 бита = 48 байт
        val key = factory.generateSecret(spec).encoded

        val aesKey = ByteArray(32)
        val iv = ByteArray(16)
        System.arraycopy(key, 0, aesKey, 0, 32)
        System.arraycopy(key, 32, iv, 0, 16)

        return aesKey to iv
    }

    private suspend fun saveFileToDownloads(tempFile: File, fileName: String): Uri = withContext(Dispatchers.IO) {
        val resolver = requireContext().contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IOException("Не удалось создать запись в MediaStore")

        resolver.openOutputStream(uri)?.use { outputStream ->
            FileInputStream(tempFile).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IOException("Не удалось записать файл в Downloads")

        return@withContext uri
    }

    private fun onSuccess() {
        progressBar.visibility = View.GONE
        tvStatus.text = "Успешно сохранено в Загрузки!"
        btnDecrypt.isEnabled = true

        val fileName = finalOutputUri?.let { getFileName(it) } ?: "файл"
        Toast.makeText(requireContext(), "Расшифровано: $fileName", Toast.LENGTH_LONG).show()

        // Опционально: кнопка "Открыть"
        tvStatus.setOnClickListener {
            finalOutputUri?.let { uri ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "text/plain")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(requireContext(), "Нет приложения для открытия", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onError(e: Exception) {
        progressBar.visibility = View.GONE
        tvStatus.text = "Ошибка: ${e.message}"
        btnDecrypt.isEnabled = true
        Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
    }

    abstract class SimpleTextWatcher : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }
}