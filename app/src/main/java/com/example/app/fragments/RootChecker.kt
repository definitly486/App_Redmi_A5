@file:Suppress("SpellCheckingInspection")

package com.example.app.fragments

import android.content.Context
import android.content.pm.PackageManager
import java.io.File

object RootChecker {
    fun hasRootAccess(context: Context): Boolean {
        return checkSuBinary() || checkSuperUserApps(context)
    }



    private fun checkSuBinary(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/bin/sh", "-c", "su -c id"))
            val result = process.waitFor()
            result == 0
        } catch (_: Exception) {
            false
        }
    }

    private fun checkSuperUserApps(context: Context): Boolean {
        val packageNames = arrayOf(
            "com.noshufou.android.su",
            "eu.chainfire.supersu",
            "com.topjohnwu.magisk"
        )

        for (pkg in packageNames) {
            try {
                context.packageManager.getApplicationInfo(pkg, 0)
                return true
            } catch (_: PackageManager.NameNotFoundException) {}
        }
        return false
    }

     fun checkWriteAccess(path: String): Boolean {
        return try {
            val testFile = File("$path/.write_test")
            if (testFile.exists()) testFile.delete()
            // Используем Runtime.exec для вызова команды 'touch'
            val command = arrayOf("su", "-c", "touch ${testFile.absolutePath}")
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()

            // Проверяем успешность операции
            when (process.exitValue()) {
                0 -> true // Файл успешно создан
                else -> false // Ошибка при создании файла
            }.also { testFile.delete() }
        } catch (_: Exception) {
            false
        }
    }

}