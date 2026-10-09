package com.example.pageswitching

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SecActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sec)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvReceived = findViewById<TextView>(R.id.tvReceived)
        val etReply = findViewById<EditText>(R.id.etReply)
        val btnReturn = findViewById<Button>(R.id.btnReturn)

        // Get data from intent extra
        val receivedText = intent.getStringExtra("input_key")
        if (!receivedText.isNullOrEmpty()) {
            tvReceived.text = receivedText
        }

        btnReturn.setOnClickListener {
            val replyText = etReply.text.toString()
            val resultIntent = Intent().apply {
                putExtra("reply_key", replyText)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}