package com.example.app.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.app.R
import com.example.app.terminal.TerminalController

class TerminalFragment : Fragment(R.layout.fragment_terminal) {

    private lateinit var output: TextView
    private lateinit var input: EditText
    private lateinit var scroll: ScrollView

    private val controller = TerminalController()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views using the fragment's view
        output = view.findViewById(R.id.output)
        input = view.findViewById(R.id.input)
        scroll = view.findViewById(R.id.scroll)

        // Set up the output callback for the controller
        controller.onOutput = { text ->
            if (text == "\u000C") {  // If the text is a clear screen command
                output.text = ""
            } else {
                output.append(text + "\n")
            }
            // Scroll to the bottom of the ScrollView
            scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
        }

        // Set focus change listener for the input field
        input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                scroll.post { scroll.smoothScrollTo(0, input.bottom) }
            }
        }

        // Set up the input action listener (for when the user presses 'Enter')
        input.setOnEditorActionListener { _, _, _ ->
            val command = input.text.toString()
            input.text.clear()  // Clear input after command is entered
            controller.execute(command)
            true
        }

        // Print a welcome message when the fragment is created
        controller.printWelcome()
    }
}
