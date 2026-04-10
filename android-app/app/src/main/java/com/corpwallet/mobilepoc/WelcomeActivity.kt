package com.corpwallet.mobilepoc

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    private lateinit var btnLoginTop: TextView
    private lateinit var btnEnter: TextView
    private lateinit var btnDemoText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.parseColor("#040B18")
        window.navigationBarColor = Color.parseColor("#040B18")

        setContentView(R.layout.activity_welcome)

        btnLoginTop = findViewById(R.id.btnLoginTop)
        btnEnter = findViewById(R.id.btnEnter)
        btnDemoText = findViewById(R.id.btnDemoText)

        btnLoginTop.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnEnter.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnDemoText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
