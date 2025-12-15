package com.example.app.terminal

class TerminalController {

    var onOutput: ((String) -> Unit)? = null
    private val registry = CommandRegistry()

    fun printWelcome() {
        onOutput?.invoke("Android Terminal v1.0")
        onOutput?.invoke("Type 'help' for commands")
    }

    fun execute(input: String) {
        if (input.isBlank()) return

        onOutput?.invoke("> $input")

        val parts = input.split(" ")
        val command = parts.first()
        val args = parts.drop(1)

        val result = registry.execute(command, args)
        onOutput?.invoke(result)
    }
}