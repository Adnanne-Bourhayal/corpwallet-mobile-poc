package com.corpwallet.mobilepoc

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SecurityActivity : AppCompatActivity() {

    private lateinit var tvTrustScore: TextView
    private lateinit var tvTrustStatus: TextView
    private lateinit var tvSignals: TextView
    private lateinit var tvRiskLevel: TextView
    private lateinit var tvRuntimeLabel: TextView

    private lateinit var navDashboard: LinearLayout
    private lateinit var navTransactions: LinearLayout
    private lateinit var navSecurity: LinearLayout
    private lateinit var navSettings: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.parseColor("#040B18")
        window.navigationBarColor = Color.parseColor("#040B18")

        setContentView(R.layout.activity_security)

        tvTrustScore = findViewById(R.id.tvTrustScore)
        tvTrustStatus = findViewById(R.id.tvTrustStatus)
        tvSignals = findViewById(R.id.tvSignals)
        tvRiskLevel = findViewById(R.id.tvRiskLevel)
        tvRuntimeLabel = findViewById(R.id.tvRuntimeLabel)

        navDashboard = findViewById(R.id.navDashboard)
        navTransactions = findViewById(R.id.navTransactions)
        navSecurity = findViewById(R.id.navSecurity)
        navSettings = findViewById(R.id.navSettings)

        renderSecurityState()

        navDashboard.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        navTransactions.setOnClickListener {
            startActivity(Intent(this, TransactionsActivity::class.java))
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

    private fun renderSecurityState() {
        val debugger = android.os.Debug.isDebuggerConnected()
        val emulator = isEmulator()
        val rooted = isRooted()
        val score = getRealTrustScore()

        tvTrustScore.text = score.toString()

        when {
            score < 40 -> {
                tvTrustStatus.text = "Critical risk environment detected"
                tvRiskLevel.text = "Risk level: Critical"
                tvRiskLevel.setTextColor(Color.parseColor("#FF5C5C"))
                tvRuntimeLabel.text = "Runtime: Restricted"
            }
            score < 70 -> {
                tvTrustStatus.text = "Elevated risk context detected"
                tvRiskLevel.text = "Risk level: Medium"
                tvRiskLevel.setTextColor(Color.parseColor("#FFD166"))
                tvRuntimeLabel.text = "Runtime: Limited trust"
            }
            else -> {
                tvTrustStatus.text = "Secure runtime environment verified"
                tvRiskLevel.text = "Risk level: Low"
                tvRiskLevel.setTextColor(Color.parseColor("#2DE1A7"))
                tvRuntimeLabel.text = "Runtime: Verified"
            }
        }

        tvSignals.text =
            "Debugger: ${if (debugger) "Detected" else "Not detected"}\n" +
            "Emulator: ${if (emulator) "Detected" else "Not detected"}\n" +
            "Root: ${if (rooted) "Detected" else "Not detected"}"
    }
}
