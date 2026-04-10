package com.corpwallet.mobilepoc

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PoliciesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#05070B")
        window.navigationBarColor = Color.parseColor("#05070B")
        setContentView(R.layout.activity_policies)
    }
}
