import android.content.Context
import android.os.Environment
import com.example.app.terminal.CommandRegistry
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.io.File
import kotlin.concurrent.thread

class TerminalController(private val context: Context) {

    var onOutput: ((String) -> Unit)? = null
    private val registry = CommandRegistry()

    fun printWelcome() {
        onOutput?.invoke("Android Shell Terminal v1.0")
        onOutput?.invoke("Type 'help' for available commands")

        // Переход в директорию приложения (Downloads)
        val appDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        appDirectory?.let {
            onOutput?.invoke("Changing to directory: ${it.absolutePath}")
        }
    }

    fun execute(input: String) {
        if (input.isBlank()) return

        onOutput?.invoke("> $input")

        // If input starts with 'su', it's a root command
        if (input.startsWith("su ")) {
            val command = input.removePrefix("su ").trim()
            executeRootCommand(command)
        } else {
            // Execute registered commands from CommandRegistry
            val parts = input.split(" ")
            val command = parts.first()
            val args = parts.drop(1)

            val result = registry.execute(command, args)
            if (result.isNotBlank()) {
                onOutput?.invoke(result)
            } else {
                // Otherwise, execute as a shell command
                executeShellCommand(input)
            }
        }
    }

    // Выполнение команды с правами суперпользователя
    fun executeRootCommand(command: String) {
        if (!isRootAvailable()) {
            onOutput?.invoke("Root access is not available.")
            return
        }

        thread {
            try {
                // Запуск процесса с правами суперпользователя
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)
                os.writeBytes("$command\n")   // Отправка команды для выполнения
                os.writeBytes("exit\n")       // Завершаем процесс после выполнения команды
                os.flush()

                // Чтение и вывод результата команды
                val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
                val errorOutput = BufferedReader(InputStreamReader(process.errorStream)).readText()

                if (output.isNotBlank()) {
                    onOutput?.invoke(output)
                }
                if (errorOutput.isNotBlank()) {
                    onOutput?.invoke(errorOutput)
                }

            } catch (e: Exception) {
                onOutput?.invoke("Error executing root command: ${e.message}")
            }
        }
    }

    // Выполнение обычной shell команды
    private fun executeShellCommand(command: String) {
        thread {
            try {
                // Получаем путь к директории приложения
                val appDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                appDirectory?.let {
                    // Запускаем команду в этой директории
                    val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "cd ${it.absolutePath} && $command"))

                    val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
                    val errorOutput = BufferedReader(InputStreamReader(process.errorStream)).readText()

                    if (output.isNotBlank()) {
                        onOutput?.invoke(output)
                    }
                    if (errorOutput.isNotBlank()) {
                        onOutput?.invoke(errorOutput)
                    }
                }
            } catch (e: Exception) {
                onOutput?.invoke("Error executing command: ${e.message}")
            }
        }
    }

    // Проверка доступности прав суперпользователя
    private fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("which su")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            result != null && result.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}
