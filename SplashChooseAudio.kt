package com.sup.dev.android.views.splash

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.sup.dev.android.androiddevsup.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.splash.view.SplashViewDialog
import com.sup.dev.android.views.splash.view.SplashViewSheet
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsThreads
import java.io.File
import java.io.InputStream

class SplashChooseAudio : SplashRecycler(R.layout.splash_choose_audio) {
    private val myAdapter: RecyclerCardAdapter = RecyclerCardAdapter()
    private var onSelected: (ByteArray) -> Unit = { _ -> }
    private var audioLoaded = false
    private val addedHash = SparseArray<Boolean>()
    private val contentUri = MediaStore.Files.getContentUri("external")
    private val contentResolver = SupAndroid.appContext!!.contentResolver




    init {
        vRecycler.layoutManager = LinearLayoutManager(view.context)
        ToolsView.setRecyclerAnimation(vRecycler)


        setAdapter<SplashRecycler>(myAdapter)

        ToolsThreads.timerMain(4000) {
            if (!audioLoaded) return@timerMain
            if (isHided()) it.unsubscribe()
            else loadAudioNow()
        }

    }

    override fun onShow() {
        super.onShow()
        loadAudio()

        (vRecycler.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 0)
        vRecycler.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        if (viewWrapper is SplashViewDialog)
            (vRecycler.layoutParams as ViewGroup.MarginLayoutParams).setMargins(ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(2).toInt(), ToolsView.dpToPx(8).toInt(), 0)
        else if (viewWrapper is SplashViewSheet)
            vRecycler.layoutParams.height = ToolsView.dpToPx(320).toInt()
    }

    private fun loadAudio() {
        if (audioLoaded) return

        ToolsThreads.main(true) { vRecycler.requestLayout() }   //  Костыль. Иначе улетают кнопки вверх

        ToolsPermission.requestReadPermission({
            audioLoaded = true
            loadAudioNow()
        }, {
            ToolsToast.show(SupAndroid.TEXT_ERROR_PERMISSION_FILES)
            hide()
        })

    }

    private fun loadAudioNow() {
        val offset = myAdapter.size()
        var addCount = 0
        val cursor = SupAndroid.appContext!!.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null,
                MediaStore.Audio.Media.DATE_MODIFIED + " DESC")

        while (cursor != null && cursor.moveToNext()) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            var mediaName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
            //Получаем название файла из его пути
            mediaName = mediaName.substring(mediaName.lastIndexOf("/") + 1)
            val file = File(cursor.getString(column_index))
            val hash = file.hashCode()

            if (addedHash.get(hash) != null) break
            addCount++
            addedHash.put(hash, true)
            val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID))
            myAdapter.add(myAdapter.size() - offset, CardAudio(mediaName,
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)), id))
        }

        if (addCount > 0 && offset > 0) {
            vRecycler.scrollToPosition(0)
        }
    }

    private inner class CardAudio(val name: String, val data: String, val id: Long) : Card(R.layout.splash_choose_audio_card) {

        override fun bindView(view: View) {
            super.bindView(view)
            val vAudio: ViewText = view.findViewById(R.id.vAudio)
            vAudio.text = name
            vAudio.setOnClickListener { onClick() }
        }

        fun onClick() {
            try {
                val inputStream = contentResolver.openInputStream(ContentUris.withAppendedId(contentUri, id))
                val size: Int = inputStream?.available()!!
                val bytes = ByteArray(size)
                inputStream.read(bytes)
                inputStream.close()
                onSelected.invoke(bytes)
            } catch (e: Exception) {
                try {
                    val inputStream: InputStream = contentResolver.openInputStream(Uri.fromFile(File(data)))!!
                    val size: Int = inputStream.available()
                    val bytes = ByteArray(size)
                    inputStream.read(bytes)
                    inputStream.close()
                    onSelected.invoke(bytes)
                } catch (e: Exception) {
                    err(e)
                }
            }
            hide()
        }
    }

    fun setOnSelected(onSelected: (ByteArray) -> Unit): SplashChooseAudio {
        this.onSelected = onSelected
        return this
    }
}