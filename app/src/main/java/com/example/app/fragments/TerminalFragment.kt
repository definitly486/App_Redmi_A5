package com.example.app.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import com.example.app.R
import com.example.app.terminal.TerminalController

class TerminalFragment : Fragment() {

    private lateinit var output: TextView
    private lateinit var input: EditText
    private lateinit var scroll: ScrollView

    private val controller = TerminalController()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        output = view.findViewById(R.id.output)
        input = view.findViewById(R.id.input)
        scroll = view.findViewById(R.id.scroll)

        controller.onOutput = { text ->
            output.append(text + "\n")
            scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
        }

        input.setOnEditorActionListener { _, _, _ ->
            val command = input.text.toString()
            input.text.clear()
            controller.execute(command)
            true
        }

        controller.printWelcome()
    }
}