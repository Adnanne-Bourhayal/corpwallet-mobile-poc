package com.corpwallet.mobilepoc

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var navDashboard: LinearLayout
    private lateinit var navTransactions: LinearLayout
    private lateinit var navSecurity: LinearLayout
    private lateinit var navSettings: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.parseColor("#040B18")
        window.navigationBarColor = Color.parseColor("#040B18")

        setContentView(R.layout.activity_settings)

        navDashboard = findViewById(R.id.navDashboard)
        navTransactions = findViewById(R.id.navTransactions)
        navSecurity = findViewById(R.id.navSecurity)
        navSettings = findViewById(R.id.navSettings)

        navDashboard.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        navTransactions.setOnClickListener {
            startActivity(Intent(this, TransactionsActivity::class.java))
        }

        navSecurity.setOnClickListener {
            startActivity(Intent(this, SecurityActivity::class.java))
        }
    }
}
