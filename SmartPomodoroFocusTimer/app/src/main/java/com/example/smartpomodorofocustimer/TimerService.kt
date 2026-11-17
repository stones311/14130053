package com.example.smartpomodorofocustimer

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.widget.Toast

class TimerService : Service() {

    private var isRunning = false
    private var totalSeconds = 25 * 60 // Default 25 minutes
    private var currentSeconds = totalSeconds
    private var timerThread: Thread? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_ON) {
                if (isRunning) {
                    val remainingMinutes = (currentSeconds + 59) / 60
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.keep_focused, remainingMinutes),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenReceiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val duration = intent?.getIntExtra("duration", 25 * 60) ?: (25 * 60)
        if (!isRunning) {
            totalSeconds = duration
            currentSeconds = totalSeconds
            startCountdown()
        }
        return START_NOT_STICKY
    }

    private fun startCountdown() {
        isRunning = true
        timerThread = Thread {
            try {
                while (isRunning && currentSeconds > 0) {
                    Thread.sleep(1000)
                    currentSeconds--

                    val intent = Intent("TIMER_UPDATE").apply {
                        setPackage(packageName)
                        putExtra("remaining_seconds", currentSeconds)
                    }
                    sendBroadcast(intent)
                }

                if (currentSeconds <= 0 && isRunning) {
                    // Time's up, launch FinishActivity
                    val finishIntent = Intent(this, FinishActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(finishIntent)
                }
            } catch (e: InterruptedException) {
                // Thread interrupted
            } finally {
                stopSelf()
            }
        }
        timerThread?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        timerThread?.interrupt()
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
