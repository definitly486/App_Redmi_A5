package com.example.app.terminal

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

class TerminalController {

    var onOutput: ((String) -> Unit)? = null
    private val registry = CommandRegistry()

    fun printWelcome() {
        onOutput?.invoke("Android Shell Terminal v1.0")
        onOutput?.invoke("Type 'help' for available commands")
    }

    fun execute(input: String) {
        if (input.isBlank()) return

        onOutput?.invoke("> $input")

        // Handle shell commands
        if (input.startsWith("shell ")) {
            val command = input.removePrefix("shell ").trim()
            executeShellCommand(command)
        } else if (input.startsWith("su ")) {
            val command = input.removePrefix("su ").trim()
            executeRootCommand(command)
        } else {
            // Handle registered commands
            val parts = input.split(" ")
            val command = parts.first()
            val args = parts.drop(1)

            val result = registry.execute(command, args)
            onOutput?.invoke(result)
        }
    }

    // Executes a root command using 'su'
    fun executeRootCommand(command: String) {
        if (!isRootAvailable()) {
            onOutput?.invoke("Root access is not available.")
            return
        }

        try {
            // Execute the root command via 'su'
            val process = Runtime.getRuntime().exec("su") // Start process as root
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")   // Send command to be executed
            os.writeBytes("exit\n")       // Exit after command execution
            os.flush()

            // Read the output from the command
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

    // Executes a normal shell command
    private fun executeShellCommand(command: String) {
        try {
            val process = Runtime.getRuntime().exec(command)
            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val errorOutput = BufferedReader(InputStreamReader(process.errorStream)).readText()

            if (output.isNotBlank()) {
                onOutput?.invoke(output)
            }
            if (errorOutput.isNotBlank()) {
                onOutput?.invoke(errorOutput)
            }

        } catch (e: Exception) {
            onOutput?.invoke("Error executing command: ${e.message}")
        }
    }

    // Check if root is available
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
