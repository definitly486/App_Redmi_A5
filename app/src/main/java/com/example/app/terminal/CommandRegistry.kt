package com.example.app.terminal

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommandRegistry {

    private val commands = mutableMapOf<String, (List<String>) -> String>()

    init {
        registerDefaults()
    }

    private fun registerDefaults() {

        commands["help"] = {
            """
            Available commands:
            help
            clear
            echo <text>
            time
            """.trimIndent()
        }

        commands["echo"] = { args ->
            args.joinToString(" ")
        }

        commands["time"] = {
            SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(Date())
        }

        commands["clear"] = {
            "\u000C" // спец-маркер
        }
    }

    fun execute(command: String, args: List<String>): String {
        val cmd = commands[command] ?: return "Command not found: $command"
        return cmd(args)
    }
}
