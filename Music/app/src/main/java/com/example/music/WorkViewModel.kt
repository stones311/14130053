package com.example.music

import android.app.Application
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class WorkViewModel(application: Application) : AndroidViewModel(application) {
    private val handlerThread = HandlerThread("VM-Work").apply { start() }
    private val worker = Handler(handlerThread.looper)

    private val _progress = MutableLiveData(0)
    val progress: LiveData<Int> = _progress

    private val _status = MutableLiveData("Idle")
    val status: LiveData<String> = _status

    @Volatile private var running = false
    private var mediaPlayer: MediaPlayer? = null

    private fun startMusic() {
        try {
            stopMusic()
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mediaPlayer = MediaPlayer.create(getApplication(), uri)?.apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopMusic() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }

    private fun sleepWithCheck(millis: Long): Boolean {
        val interval = 100L
        var elapsed = 0L
        while (elapsed < millis) {
            if (!running) return false
            val sleepTime = minOf(interval, millis - elapsed)
            try {
                Thread.sleep(sleepTime)
            } catch (_: InterruptedException) {
                return false
            }
            elapsed += sleepTime
        }
        return running
    }

    fun start() {
        if (running) return
        running = true
        _status.postValue("準備中…")
        _progress.postValue(0)

        worker.post {
            try {
                // 1. 準備工作階段 (顯示 "準備中…")
                if (!sleepWithCheck(2000)) {
                    _status.postValue("背景工作中斷")
                    stopMusic()
                    return@post
                }

                // 2. 開始背景工作階段 (顯示 "工作中" 並且播放背景音樂)
                _status.postValue("工作中")
                startMusic()

                for (i in 1..100) {
                    if (!sleepWithCheck(100)) break
                    _progress.postValue(i)
                }

                // 3. 背景工作結束或中斷
                if (running) {
                    _status.postValue("背景工作結束")
                } else {
                    _status.postValue("背景工作中斷")
                }
            } catch (_: InterruptedException) {
                _status.postValue("背景工作中斷")
            } finally {
                running = false
                stopMusic()
            }
        }
    }

    fun cancel() {
        running = false
        stopMusic()
    }

    override fun onCleared() {
        running = false
        stopMusic()
        handlerThread.quitSafely()
        super.onCleared()
    }
}
