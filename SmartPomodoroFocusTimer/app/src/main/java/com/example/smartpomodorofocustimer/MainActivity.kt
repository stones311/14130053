package com.example.smartpomodorofocustimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "TIMER_UPDATE") {
                val remainingSeconds = intent.getIntExtra("remaining_seconds", 0)
                updateTimerDisplay(remainingSeconds)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvTimer = findViewById(R.id.tvTimer)
        tvStatus = findViewById(R.id.tvStatus)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        btnStart.setOnClickListener {
            val intent = Intent(this, TimerService::class.java).apply {
                putExtra("duration", 25 * 60) // 25 minutes
            }
            startService(intent)
            tvStatus.text = getString(R.string.status_running)
            btnStart.isEnabled = false
        }

        btnStop.setOnClickListener {
            val intent = Intent(this, TimerService::class.java)
            stopService(intent)
            tvStatus.text = getString(R.string.status_stopped)
            tvTimer.text = getString(R.string.default_time)
            btnStart.isEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("TIMER_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timerReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(timerReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(timerReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }

    private fun updateTimerDisplay(totalSeconds: Int) {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        tvTimer.text = String.format("%02d:%02d", minutes, seconds)
        if (totalSeconds <= 0) {
            tvStatus.text = getString(R.string.status_ready)
            btnStart.isEnabled = true
        }
    }
}
