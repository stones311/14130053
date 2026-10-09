package com.example.pageswitching

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private lateinit var etInput: EditText

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val replyText = result.data?.getStringExtra("reply_key")
            if (!replyText.isNullOrEmpty()) {
                tvResult.text = replyText
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

        etInput = findViewById(R.id.etInput)
        tvResult = findViewById(R.id.tvResult)
        val btnSwitch = findViewById<Button>(R.id.btnSwitch)

        btnSwitch.setOnClickListener {
            val textToSend = etInput.text.toString()
            val intent = Intent(this, SecActivity::class.java).apply {
                putExtra("input_key", textToSend)
            }
            startForResult.launch(intent)
        }
    }
}