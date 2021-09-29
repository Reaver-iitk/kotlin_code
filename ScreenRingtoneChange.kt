package ru.beward.intercom.ui.app_settings.ringtone

import android.media.AudioManager
import android.os.Environment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsFilesAndroid
import com.sup.dev.android.tools.ToolsPermission
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.utils.UtilsMediaPlayer
import com.sup.dev.android.views.splash.SplashChooseAudio
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.java.libs.debug.log
import ru.beward.intercom.R
import ru.beward.intercom.controllers.app.ControllerAppSettings
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel

class ScreenRingtoneChange : Screen(R.layout.screen_settings_bell_change) {

    companion object{
        val KEY_CUSTOM_RINGTONES = "KEY_CUSTOM_RINGTONES"
    }

    private val vAdd: View = findViewById(R.id.vAdd)
    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)

    private val utilsMediaPlayer = UtilsMediaPlayer()
    private val adapter = RecyclerCardAdapter()
    private val root = Environment.getExternalStorageDirectory().absolutePath + "/Android/data/" + SupAndroid.appContext!!.packageName
    private val folder = File("$root/ringtone/")

    init {
        vAdd.setOnClickListener { addCustomRingtone() }
        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter
        utilsMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)

        loadRingtones()
    }


    override fun onStop() {
        utilsMediaPlayer.stop()
        super.onStop()
    }

    private fun loadRingtones() { //  Стандартные
        adapter.clear()
        val items = arrayOf(ToolsResources.s(R.string.settings_ringtone_1), ToolsResources.s(R.string.settings_ringtone_2))
        for (ringtoneName in items)
            adapter.add(CardRingtone(this, ringtoneName, "android.resource://" + context.getPackageName() + "/raw/" + ringtoneName.toLowerCase()))


        //  Добавленные пользователем
        try {
            folder.mkdirs()
        } catch (e: SecurityException) {
        }
        val files: Array<File> = folder.listFiles()
        if (files.isNotEmpty() == true) {
            for (file in files) adapter.add(CardRingtone(this, file.name, file.path))
        }

        //  Отметка выбранной
        val selectedPath = ControllerAppSettings.ringtonePatch
        for (c in adapter.get(CardRingtone::class))
            if (c.path == selectedPath) {
                c.setChecked(true)
                break
            }
    }

    fun onSelectedChange(card: CardRingtone) {
        utilsMediaPlayer.play(card.path)
        ControllerAppSettings.ringtonePatch = card.path
    }


    private fun addCustomRingtone() {
        utilsMediaPlayer.stop()
        ToolsPermission.requestReadPermission {
            SplashChooseAudio()
                    .setOnSelected { file ->
                        ToolsFilesAndroid.writeFile(folder.absolutePath + "/" + "Custom ringtone",file)
                        loadRingtones()
                    }
                    .setTitle(R.string.settings_ringtone_select)
                    .asDialogShow()
        }
    }


}