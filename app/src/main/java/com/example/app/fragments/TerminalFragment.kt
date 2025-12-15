package com.example.app.fragments

import TerminalController
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.app.R


class TerminalFragment : Fragment(R.layout.fragment_terminal) {

    private lateinit var output: TextView
    private lateinit var input: EditText
    private lateinit var scroll: ScrollView

    private lateinit var controller: TerminalController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Передаем контекст в TerminalController при присоединении фрагмента
        controller = TerminalController(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация представлений (UI)
        output = view.findViewById(R.id.output)
        input = view.findViewById(R.id.input)
        scroll = view.findViewById(R.id.scroll)

        // Инициализируем обработчик вывода сразу после создания контроллера
        controller.onOutput = { text ->
            if (text == "\u000C") {  // Если это команда очистки экрана
                output.text = ""
            } else {
                output.append(text + "\n")
            }
            // Прокручиваем ScrollView вниз, чтобы показать последний вывод
            scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
        }

        // Слушатель для ввода текста (при нажатии Enter)
        input.setOnEditorActionListener { _, _, _ ->
            val command = input.text.toString()
            input.text.clear()  // Очищаем поле ввода после команды
            controller.execute(command)
            true
        }

        // Выводим приветственное сообщение, когда фрагмент создается
        controller.printWelcome()
    }
}
