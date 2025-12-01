
import android.util.Log
import kotlinx.coroutines.*
import java.io.*
import java.util.concurrent.TimeoutException

object ShellExecutor {

    private const val TAG = "ShellExecutor"
    private var isRootAvailable: Boolean? = null

    // Попробуем найти рабочий путь к su (поддержка Magisk, KernelSU, APatch и т.д.)
    private val possibleSuPaths = arrayOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/system/sbin/su",
        "/sbin/su",
        "/su/bin/su",
        "/magisk/.magisk/su", // не прямой путь, но иногда работает через symlink
        "/data/adb/magisk/su",
        "su" // просто "su" — часто работает
    )

    private fun getSuPath(): String {
        for (path in possibleSuPaths) {
            if (File(path).exists() || path == "su") {
                return path
            }
        }
        return "su" // fallback
    }

    fun hasRoot(): Boolean {
        if (isRootAvailable != null) return isRootAvailable!!
        return try {
            val process = Runtime.getRuntime().exec("su")
            process.outputStream.bufferedWriter().use {
                it.write("id\n")
                it.write("exit\n")
                it.flush()
            }
            val result = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()
            process.destroy()
            val root = result == 0 && output.contains("uid=0")
            isRootAvailable = root
            root
        } catch (e: Exception) {
            isRootAvailable = false
            false
        }
    }

    /**
     * Выполняет команду в shell (с root, если доступен)
     */
    fun exec(
        command: String,
        timeoutMs: Long = 30_000L,
        onResult: (success: Boolean, output: String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = execInternal(command, timeoutMs)
            withContext(Dispatchers.Main) {
                onResult(result.first, result.second)
            }
        }
    }

    /**
     * Синхронная версия (осторожно! не вызывать в главном потоке)
     */
    fun execSync(command: String, timeoutMs: Long = 30_000L): Pair<Boolean, String> {
        return runBlocking { execInternal(command, timeoutMs) }
    }

    private suspend fun execInternal(command: String, timeoutMs: Long): Pair<Boolean, String> {
        return withTimeoutOrNull(timeoutMs) {
            val useRoot = hasRoot()
            val shell = if (useRoot) getSuPath() else "sh"
            Log.d(TAG, "Executing [$command] as $shell")

            var process: Process? = null
            try {
                process = Runtime.getRuntime().exec(shell)

                val outputBuilder = StringBuilder()
                val errorBuilder = StringBuilder()

                // Потоки для чтения вывода
                val outputReader = process.inputStream.bufferedReader()
                val errorReader = process.errorStream.bufferedReader()

                // Запись команды
                process.outputStream.bufferedWriter().use { writer ->
                    writer.write(command + "\n")
                    writer.write("exit\n")
                    writer.flush()
                }

                // Читаем вывод параллельно
                val outputJob = launch { outputReader.forEachLine { outputBuilder.append(it).append("\n") } }
                val errorJob = launch { errorReader.forEachLine { errorBuilder.append(it).append("\n") } }

                val exitCode = process.waitFor()
                outputJob.join()
                errorJob.join()

                val fullOutput = "STDOUT:\n${outputBuilder}\nSTDERR:\n${errorBuilder}".trim()

                Log.d(TAG, "Command exit code: $exitCode")
                if (exitCode != 0) Log.e(TAG, fullOutput)

                Pair(exitCode == 0, fullOutput)
            } catch (e: Exception) {
                Log.e(TAG, "Shell execution failed", e)
                Pair(false, e.message ?: "Unknown error")
            } finally {
                process?.destroy()
            }
        } ?: Pair(false, "Timeout after ${timeoutMs}ms")
    }

    // Удобные однострочники (если не нужен вывод)
    fun execSimple(command: String) {
        exec(command) { _, _ -> }
    }

    fun execRoot(command: String, onResult: ((Boolean) -> Unit)? = null) {
        if (!hasRoot()) {
            onResult?.invoke(false)
            return
        }
        exec(command) { success, _ -> onResult?.invoke(success) }
    }
}