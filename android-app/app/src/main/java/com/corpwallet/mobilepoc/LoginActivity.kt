package com.corpwallet.mobilepoc

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvLoginError: TextView
    private lateinit var btnLogin: TextView
    private lateinit var btnDemoAccess: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.parseColor("#05070B")
        window.navigationBarColor = Color.parseColor("#05070B")

        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        tvLoginError = findViewById(R.id.tvLoginError)
        btnLogin = findViewById(R.id.btnLogin)
        btnDemoAccess = findViewById(R.id.btnDemoAccess)

        btnLogin.setOnClickListener {
            handleLogin()
        }

        btnDemoAccess.setOnClickListener {
            etUsername.setText("admin")
            etPassword.setText("admin")
            handleLogin()
        }
    }

    private fun handleLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username == "admin" && password == "admin") {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            tvLoginError.text = "Invalid credentials. Please use the demo workspace access."
        }
    }
}
