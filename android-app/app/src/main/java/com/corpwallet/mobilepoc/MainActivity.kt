package com.corpwallet.mobilepoc

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnLogout: TextView

    private lateinit var navDashboard: LinearLayout
    private lateinit var navTransactions: LinearLayout
    private lateinit var navSecurity: LinearLayout
    private lateinit var navSettings: LinearLayout

    private lateinit var ringChart: RingChartView
    private lateinit var tvRingPercent: TextView
    private lateinit var tvUsedBadge: TextView
    private lateinit var tvTrustMetric: TextView
    private lateinit var tvRiskLevel: TextView
    private lateinit var tvSecurityHeadline: TextView
    private lateinit var tvSecuritySubline: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.parseColor("#040B18")
        window.navigationBarColor = Color.parseColor("#040B18")

        setContentView(R.layout.activity_main)

        btnLogout = findViewById(R.id.btnLogout)

        navDashboard = findViewById(R.id.navDashboard)
        navTransactions = findViewById(R.id.navTransactions)
        navSecurity = findViewById(R.id.navSecurity)
        navSettings = findViewById(R.id.navSettings)

        ringChart = findViewById(R.id.ringChart)
        tvRingPercent = findViewById(R.id.tvRingPercent)
        tvUsedBadge = findViewById(R.id.tvUsedBadge)
        tvTrustMetric = findViewById(R.id.tvTrustMetric)
        tvRiskLevel = findViewById(R.id.tvRiskLevel)
        tvSecurityHeadline = findViewById(R.id.tvSecurityHeadline)
        tvSecuritySubline = findViewById(R.id.tvSecuritySubline)

        ringChart.setData(listOf(32f, 68f))
        tvRingPercent.text = "32%"
        tvUsedBadge.text = "32% used"

        val trustScore = getRealTrustScore()
        tvTrustMetric.text = "$trustScore /100"

        when {
            trustScore < 40 -> {
                tvRiskLevel.text = "Critical"
                tvRiskLevel.setTextColor(Color.parseColor("#FF5C5C"))
                tvSecurityHeadline.text = "Critical"
                tvSecuritySubline.text = "Compromised runtime detected"
            }
            trustScore < 70 -> {
                tvRiskLevel.text = "Medium"
                tvRiskLevel.setTextColor(Color.parseColor("#FFD166"))
                tvSecurityHeadline.text = "Warning"
                tvSecuritySubline.text = "Elevated risk environment"
            }
            else -> {
                tvRiskLevel.text = "Low"
                tvRiskLevel.setTextColor(Color.parseColor("#2DE1A7"))
                tvSecurityHeadline.text = "Secure"
                tvSecuritySubline.text = "All systems operational"
            }
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        navTransactions.setOnClickListener {
            startActivity(Intent(this, TransactionsActivity::class.java))
        }

        navSecurity.setOnClickListener {
            startActivity(Intent(this, SecurityActivity::class.java))
        }

        navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.BRAND.startsWith("generic") ||
                Build.DEVICE.startsWith("generic")
    }

    private fun isRooted(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/app/Superuser.apk"
        )
        return paths.any { java.io.File(it).exists() }
    }

    private fun getRealTrustScore(): Int {
        var score = 100
        if (android.os.Debug.isDebuggerConnected()) score -= 40
        if (isEmulator()) score -= 30
        if (isRooted()) score -= 30
        return score.coerceAtLeast(0)
    }
}
