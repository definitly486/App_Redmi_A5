package com.example.app

import java.io.File

object KernelSUInstaller {

    private const val MODULE_NAME = "APatch-KSU.zip"
    private const val DOWNLOAD_PATH = "/storage/emulated/0/Download"

    @Suppress("SpellCheckingInspection")
    fun installAPatchKSU(): Boolean {
        val moduleFile = File("$DOWNLOAD_PATH/$MODULE_NAME")

        if (!moduleFile.exists() || !moduleFile.canRead()) {
            return false
        }

        return try {
            val process = Runtime.getRuntime().exec("su -mm")
            java.io.DataOutputStream(process.outputStream).use { os ->
                os.writeBytes("ksud module install \"${moduleFile.absolutePath}\"\n")
                os.writeBytes("exit \$?\n")
                os.flush()
            }
            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}