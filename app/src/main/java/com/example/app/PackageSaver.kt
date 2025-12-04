@file:Suppress("SpellCheckingInspection")

package com.example.app

import android.content.Context
import android.os.Environment
import java.io.FileWriter
import java.io.IOException

/**
 * Функция для записи списка пакетов в указанный файл.
 *
 * @param org.bouncycastle.crypto.params.Blake3Parameters.context Контекст приложения
 * @param filename Название файла куда записать пакеты.
 */
fun Context.savePackagesToFile(filename: String): Boolean {
    val packages = listOf(
        "com.miui.analytics.go",
        "ru.ivi.client",
        "ru.vk.store",
        "com.vk.vkvideo",
        "ru.beru.android",
        "ru.more.play",
        "ru.oneme.app",
        "com.miui.player",
        "com.miui.videoplayer",
        "com.miui.theme.lite",
        "com.miui.gameCenter.overlay",
        "com.miui.videoplayer.overlay",
        "com.miui.bugreport",
        "com.miui.cleaner.go",
        "com.miui.player.overlay",
        "com.miui.msa.global",
        "com.yandex.searchapp",
        "com.yandex.browser",
        "com.google.android.youtube",
        "com.google.android.apps.youtube.music",
        "com.android.shareMe.overlay",
        "com.vitastudio.mahjong",
        "com.oakever.tiletrip",
        "com.ordinaryjoy.woodblast",
        "com.go.browser",
        "com.facebook.appmanager",
        "com.google.android.apps.tachyon",
        "com.xiaomi.midrop",
        "com.miui.global.packageinstaller",
        "com.xiaomi.discover",
        "com.xiaomi.mipicks",
        "com.google.android.videos",
        "com.miui.android.fashiongallery",
        "com.google.android.apps.safetyhub",
        "com.google.android.overlay.gmsconfig.searchgo",
        "com.google.android.apps.searchlite",
        "com.google.android.appsearch.apk",
        "com.google.android.apps.docs",
        "com.xiaomi.glgm",
        "com.google.android.gm",
        "com.yandex.preinstallsatellite",
        "com.tencent.soter.soterserver ",
        "com.android.bookmarkprovider",
        "com.xiaomi.mipicks",
        "com.xiaomi.discover",
        "com.facebook.services",
        "com.android.bips",
        "com.facebook.system",
        "com.google.android.feedback",
        "android.autoinstalls.config.Xiaomi.model",
        "com.google.android.apps.wellbeing",
        "com.android.vending",
        "com.android.musicfx",
        "com.google.android.tts",
        "com.mi.globalminusscreen",
        "com.android.printspooler",
        "com.google.android.printservice.recommendation",
        "com.google.android.setupwizard",
        "com.android.ons",
        "com.google.android.partnersetup",
        "com.android.providers.partnerbookmarks",
        "com.mi.android.globalFileexplorer.overlay",
        "com.android.backupconfirm",
        "android.overlay.multiuser",
        "com.android.calllogbackup",
        "com.android.cameraextensions",
        "com.google.android.marvin.talkback",
        "org.ifaa.aidl.manager",
        "com.android.wallpaperbackup",
        "com.android.avatarpicker",
        "com.google.android.apps.subscriptions.red",
        "com.google.android.ext.shared",
        "com.android.sharedstoragebackup",
        "com.google.android.googlequicksearchbox",
        "com.google.android.apps.walletnfcrel",
        "com.kms.free",
        "com.google.android.apps.magazines",
        "com.google.android.apps.assistant",
        "com.yandex.searchapp",
        "com.silead.factorytest",
        "com.android.chrome",
        "com.mi.globallayout",
        "com.google.android.apps.nbu.files",
        "com.google.android.apps.maps",
        "com.android.DeviceAsWebcam",
        "com.android.dynsystem",
        "com.huaqin.sarcontroller",
        "com.android.customization.themes",
        "com.google.android.gms.supervision",
        "com.google.android.syncadapters.calendar",
        "com.google.android.apps.restore",
        "com.google.android.health.connect.backuprestore",
        "com.android.microdroid.empty_payload",
        "com.android.settings.intelligence",
        "com.google.android.overlay.devicelockcontroller",
        "com.google.android.devicelockcontroller",
        "com.android.wallpaper",
        "com.android.wallpapercropper",
        "com.android.overlay.wallpaperconfig",
        "com.bsp.logmanager",
        "com.android.traceur",
        "com.google.android.gms.location.history",
        "com.spreadtrum.proxy.nfwlocation",
        "com.miui.qr",
        "com.mi.AutoTest",
        "com.android.egg",
        "com.google.android.federatedcompute",
        "com.android.virtualmachine.res",
        "com.android.systemui.accessibility.accessibilitymenu",
        "com.android.providers.settings.auto_generated_rro_product__",
        "com.sprd.validationtools",
        "com.google.android.as.oss",
        "com.sprd.logmanager",
        "com.google.android.onetimeinitializer",
        "com.android.theme.font.notoserifsource",
        "com.google.android.overlay.modules.captiveportallogin.forframework",
        "com.sprd.uasetting",
        "com.sprd.camta",
        "com.android.rkpdapp",
        "com.google.android.ondevicepersonalization.services",
        "com.huaqin.factory",
        "com.android.overlay.gmssettings",
        "com.sprd.linkturbo",
        "com.android.role.notes.enabled",
        "com.goodix.gftest",
        "com.sprd.engineermode",
        "com.android.overlay.gmscontactprovider",
        "com.android.overlay.gmssettingprovider",
        "com.google.android.overlay.modules.permissioncontroller",
        "com.android.dreams.basic"
    ).joinToString("\n")

    val downloadFolder = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    if (downloadFolder != null && !downloadFolder.exists()) {
        downloadFolder.mkdirs()
    }

    val fullPath = "${downloadFolder?.absolutePath}/$filename"

    return try {
        FileWriter(fullPath).use { it.write(packages) }
        true
    } catch (_: IOException) {
        false
    }
}