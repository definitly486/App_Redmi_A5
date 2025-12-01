@file:Suppress("SpellCheckingInspection")

package com.example.app.fragments

import DownloadHelper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.R


class FirstFragment : Fragment() {

    private lateinit var downloadHelper: DownloadHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first, container, false)

        // Инициализация помощника для скачивания
        downloadHelper = DownloadHelper(requireContext())

        // Карта кнопок и соответствующих URL для скачивания APK
        val installButtons = mapOf(
            R.id.installfm to "https://github.com/definitly486/redmia5/releases/download/apk/FM+v3.6.3.apk",
            R.id.installtermos to "https://github.com/definitly486/redmia5/releases/download/apk/Termos_v2.4_universal.apk",
            R.id.installaurora to "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/com.aurora.store_70.apk",
            R.id.installnewpipe to "https://github.com/definitly486/redmia5/releases/download/apk/NewPipe_nightly-1068.apk",
            R.id.installjob to "https://github.com/definitly486/redmia5/releases/download/apk/ozonjob.apk",
            R.id.installtc to "https://github.com/definitly486/redmia5/releases/download/apk/Total_Commander_v.3.50d.apk",
            R.id.installsberbank to "https://github.com/definitly486/redmia5/releases/download/apk/SberbankOnline.apk",
            R.id.installozonbank to "https://github.com/definitly486/redmia5/releases/download/apk/Ozon_Bank_18.35.0.apk",
            R.id.installtelegram to "https://github.com/definitly486/redmia5/releases/download/apk/Telegram+11.14.1.apk",
            R.id.installgnucash to "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/org-gnucash-android-24003-39426726-deeea690953a751a05a1a35017540c33.apk",
            R.id.installkeychain to "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/org.sufficientlysecure.keychain_60200.apk",
            R.id.installdpi to "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/ByeByeDPI-arm64-v8a-release.apk",
            R.id.installsports to "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/sports+2024_1.2_apkcombo.com_antisplit.apk",
            R.id.installhacker to "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/Hacker_v1.41.1.apk",
            R.id.installzepp to "https://github.com/definitly486/redmia5/releases/download/apk/zepplife_6.14.0_repack.apk",
            R.id.installmpv to "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/is.xyz.mpv_41.apk",
            R.id.installray to "https://github.com/definitly486/redmia5/releases/download/apk/v2rayNG_1.10.24_arm64-v8a.apk",
            R.id.installtermosplus to "https://github.com/definitly486/redmia5/releases/download/apk/com.termoneplus_3.6.0.apk",
            R.id.installapatch to "https://github.com/definitly486/redmia5/releases/download/apk/APatch_11107_11107-release-signed.apk",
            R.id.installkernelsu to "https://github.com/definitly486/redmia5/releases/download/apk/KernelSU_v1.0.5_12081-release.apk",
            R.id.installcore to "https://github.com/definitly486/redmia5/releases/download/apk/Core+Music+Player_1.0.apk",
            R.id.installpluma to "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Pluma_.private_fast.browser_1.80_APKPure.apk",
            R.id.installtelegramx to "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Telegram+X+0.27.5.1747-arm64-v8a.apk" ,
            R.id.install3с to "https://github.com/definitly486/redmia5/releases/download/apk/3C+Task+Manager+v3.9.4+.33127.+arm64-v8a.apk",
            R.id.installk9mail to "https://github.com/definitly486/redmia5/releases/download/apk/k9mail-13.0.apk",
            R.id.installgoogleauth to "https://github.com/definitly486/redmia5/releases/download/apk/Google+Authenticator+7.0.apk",
            R.id.installyandexmap to "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Yandex_Maps_17.2.0.apk",
            R.id.installfdroid to "https://github.com/definitly486/redmia5/releases/download/apk/org.fdroid.fdroid_1018050.apk",
            R.id.installvcore to "https://github.com/definitly486/redmia5/releases/download/apk/vcore.apk",
            R.id.installelektichka to "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/yandex_elektrichka+v.3.43.0-34300_arm64.apk"
        )

        // Назначаем обработчик события каждому элементу карты
        installButtons.forEach { (buttonId, url) ->
            view.findViewById<Button>(buttonId)?.apply {
                setOnClickListener { _: View -> // явное указание типа View
                    downloadHelper.downloadApk(url) { file ->
                        if (file != null) {
                            Toast.makeText(
                                requireContext(),
                                "Файл загружен: ${file.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        downloadHelper.cleanup()
    }


}