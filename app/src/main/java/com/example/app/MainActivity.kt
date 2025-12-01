@file:Suppress("SpellCheckingInspection")

package com.example.app

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.app.fragments.FifthFragment
import com.example.app.fragments.KernelSuFragment
import com.example.app.fragments.FirstFragment
import com.example.app.fragments.NinthFragment
import com.example.app.fragments.PythonFragment
import com.example.app.fragments.RootChecker
import com.example.app.fragments.SecondFragment
import com.example.app.fragments.SeventhFragment
import com.example.app.fragments.SixthFragment
import com.example.app.fragments.TenthFragment
import com.example.app.fragments.ThirdFragment

class MainActivity : AppCompatActivity() {

    private val fragmentList = listOf(
        FirstFragment(),
        SecondFragment(),
        ThirdFragment(),
        FifthFragment(),
        SixthFragment(),
        SeventhFragment(),
        NinthFragment(),
        TenthFragment(),
        PythonFragment(),
        KernelSuFragment()
    )

    private val buttonTitles = listOf(
        "Первая", "Вторая", "Третья", "Профили", "Git Clone", "Седьмая" ,"Восьмая","Десятая","Python3","KernelSu"
    )

    private var selectedButton: Button? = null
    private lateinit var buttonsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonsContainer = findViewById(R.id.buttonsContainer)



        // Проверка root-доступа устройства
        if (RootChecker.hasRootAccess(this)) {
            Toast.makeText(this, "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(this, "Root-доступ отсутствует.", Toast.LENGTH_SHORT).show()
        }

        // Проверка возможности записи в папку '/system'
        val pathToCheck = "/system"
        if (RootChecker.checkWriteAccess(pathToCheck)) {
            Toast.makeText(
                this,
                "Запись в '$pathToCheck' возможна!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "Запись в '$pathToCheck' невозможна.",
                Toast.LENGTH_SHORT
            ).show()
        }

        //разрешить изменть системные настройки

        ShellExecutor.exec("pm grant com.example.app android.permission.WRITE_SECURE_SETTINGS") { success, output ->
            if (success) {
                Toast.makeText(this, "WRITE_SECURE_SETTINGS предоставлен", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка: $output", Toast.LENGTH_LONG).show()
            }
        }

        ShellExecutor.exec("pm grant com.example.app android.permission.WRITE_SETTINGS") { success, _ ->
            if (success) {
                Toast.makeText(this, "WRITE_SETTINGS предоставлен", Toast.LENGTH_SHORT).show()
            }
        }

        //разрешить установку из этого приложения

        // Создаём файл packages.txt
        if (savePackagesToFile("packages.txt")) {
            Toast.makeText(this, "Файл packages.txt создан.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Ошибка создания файла.", Toast.LENGTH_SHORT).show()
        }

        // Настраиваем кнопки
        setupActionButtons(savedInstanceState)

        // Открываем первый фрагмент, если первый запуск
        if (savedInstanceState == null) {
            openFragment(fragmentList[0], buttonTitles[0])
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем индекс выбранной кнопки
        val selectedIndex = selectedButton?.let { button ->
            buttonsContainer.indexOfChild(button)
        } ?: 0
        outState.putInt("SELECTED_BUTTON_INDEX", selectedIndex)
    }



    private fun setupActionButtons(savedInstanceState: Bundle?) {
        buttonsContainer.removeAllViews()

        // Определяем, какую кнопку подсветить
        val savedIndex = savedInstanceState?.getInt("SELECTED_BUTTON_INDEX", 0) ?: 0

        buttonTitles.forEachIndexed { index, title ->
            val button = Button(this).apply {
                text = title
                textSize = 13f
                minHeight = 0
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    36.dpToPx()
                ).apply {
                    setMargins(0, 0, 0, 4.dpToPx())
                }
                setPadding(10.dpToPx())
                background = ContextCompat.getDrawable(this@MainActivity, R.drawable.button_selector)
            }

            button.setOnClickListener {
                // Сбрасываем старую подсветку
                selectedButton?.isSelected = false

                // Подсвечиваем новую
                button.isSelected = true
                selectedButton = button

                openFragment(fragmentList[index], title)
            }

            buttonsContainer.addView(button)

            // Подсвечиваем нужную кнопку
            if (index == savedIndex) {
                button.isSelected = true
                selectedButton = button
            }
        }

        // Открываем нужный фрагмент при восстановлении
        if (savedIndex in fragmentList.indices) {
            openFragment(fragmentList[savedIndex], buttonTitles[savedIndex])
        }
    }

    private fun openFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .setReorderingAllowed(true)
            .addToBackStack(title)
            .commit()
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()
}