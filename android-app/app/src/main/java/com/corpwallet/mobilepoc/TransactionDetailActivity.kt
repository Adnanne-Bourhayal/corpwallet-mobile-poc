package com.corpwallet.mobilepoc

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread

class TransactionDetailActivity : AppCompatActivity() {


    private val client = OkHttpClient()

    private lateinit var tvDetailRecipient: TextView
    private lateinit var tvDetailAmount: TextView
    private lateinit var tvDetailRisk: TextView
    private lateinit var tvSecurity: TextView
    private lateinit var tvApprovalResult: TextView
    private lateinit var btnApprove: TextView
    private lateinit var btnReject: TextView
    private lateinit var rbTrusted: RadioButton
    private lateinit var rbRestricted: RadioButton

    private var transactionId: Int = -1
    private var risk: Int = 0
    private var amount: Double = 0.0
    private var recipient: String = ""


    private fun isEmulator(): Boolean {
    return (android.os.Build.FINGERPRINT.contains("generic")
            || android.os.Build.MODEL.contains("Emulator")
            || android.os.Build.MODEL.contains("Android SDK built for x86")
            || android.os.Build.MANUFACTURER.contains("Genymotion")
            || android.os.Build.BRAND.startsWith("generic")
            || android.os.Build.DEVICE.startsWith("generic"))
}

private fun isRooted(): Boolean {
    val paths = arrayOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/system/app/Superuser.apk"
    )
    return paths.any { path -> java.io.File(path).exists() }
}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#05070B")
        window.navigationBarColor = Color.parseColor("#05070B")
        setContentView(R.layout.activity_transaction_detail)

        transactionId = intent.getIntExtra("id", -1)
        recipient = intent.getStringExtra("recipient") ?: "Unknown recipient"
        amount = intent.getDoubleExtra("amount", 0.0)
        risk = intent.getIntExtra("risk", 0)

        tvDetailRecipient = findViewById(R.id.tvDetailRecipient)
        tvDetailAmount = findViewById(R.id.tvDetailAmount)
        tvDetailRisk = findViewById(R.id.tvDetailRisk)
        tvSecurity = findViewById(R.id.tvSecurity)
        tvApprovalResult = findViewById(R.id.tvApprovalResult)
        btnApprove = findViewById(R.id.btnApprove)
        btnReject = findViewById(R.id.btnReject)
        rbTrusted = findViewById(R.id.rbTrusted)
        rbRestricted = findViewById(R.id.rbRestricted)

        tvDetailRecipient.text = recipient
        tvDetailAmount.text = "€${"%.2f".format(amount)}"
        tvDetailRisk.text = "Risk Score: $risk"

        tvSecurity.text = if (risk >= 70) {
            "Runtime anomaly detected · elevated verification required"
        } else {
            "Code integrity verified · secure execution context"
        }

        tvApprovalResult.text = "Awaiting decision."

        btnApprove.setOnClickListener {
            approveTransaction()
        }

        btnReject.setOnClickListener {
            rejectTransaction()
        }
    }

 private fun getRealTrustScore(): Int {
    var score = 100

    if (android.os.Debug.isDebuggerConnected()) {
        score -= 40
    }

    if (isEmulator()) {
        score -= 30
    }

    if (isRooted()) {
        score -= 30
    }

    return score.coerceAtLeast(0)
}

    private fun approveTransaction() {
        setButtonsLoadingState(true, "Approving...")

        thread {
            val manualTrustScore = if (rbTrusted.isChecked) 80 else 30
            val simulatedTrustScore = getRealTrustScore()
            val trustScore = minOf(manualTrustScore, simulatedTrustScore)

            runOnUiThread {
              tvSecurity.text = when {
    trustScore < 40 -> "Critical risk · compromised environment detected"
    trustScore < 70 -> "Elevated risk · limited trust context"
    else -> "Secure execution environment verified"
}
            }

            val url = "http://192.168.0.192:8000/transactions/$transactionId/approve"
            val json = """{"device_trust_score":$trustScore}"""
            val body = json.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: ""

                    runOnUiThread {
                        if (response.isSuccessful && !responseBody.contains("\"error\"")) {
                            tvApprovalResult.text = "Approval accepted · backend trust policy validated."
                            goBackToQueue()
                        } else {
                            val backendMessage = try {
                                JSONObject(responseBody).optString("error", "Approval blocked")
                            } catch (_: Exception) {
                                "Approval blocked"
                            }

                            tvApprovalResult.text = "$backendMessage · restricted session detected."
                            tvSecurity.text = "Approval blocked · backend trust policy enforced"
                            setButtonsLoadingState(false, "Approve")
                        }
                    }
                }
            } catch (e: IOException) {
                runOnUiThread {
                    tvApprovalResult.text = "Network unavailable during approval request."
                    tvSecurity.text = "Network unavailable · approval could not be completed"
                    setButtonsLoadingState(false, "Approve")
                }
            }
        }
    }

    private fun rejectTransaction() {
        setButtonsLoadingState(true, "Rejecting...")

        thread {
            val url = "http://192.168.0.192:8000/transactions/$transactionId/reject"

            val request = Request.Builder()
                .url(url)
                .post("".toRequestBody(null))
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    runOnUiThread {
                        if (response.isSuccessful) {
                            tvApprovalResult.text = "Transaction rejected successfully."
                            goBackToQueue()
                        } else {
                            tvApprovalResult.text = "Rejection request failed."
                            tvSecurity.text = "Rejection request failed"
                            setButtonsLoadingState(false, "Approve")
                        }
                    }
                }
            } catch (e: IOException) {
                runOnUiThread {
                    tvApprovalResult.text = "Network unavailable during rejection request."
                    tvSecurity.text = "Network unavailable · rejection could not be completed"
                    setButtonsLoadingState(false, "Approve")
                }
            }
        }
    }

    private fun goBackToQueue() {
        val intent = Intent(this, PendingTransactionsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun setButtonsLoadingState(isLoading: Boolean, approveText: String) {
        btnApprove.isEnabled = !isLoading
        btnReject.isEnabled = !isLoading
        rbTrusted.isEnabled = !isLoading
        rbRestricted.isEnabled = !isLoading
        btnApprove.text = approveText
        if (!isLoading) {
            btnReject.text = "Reject"
        }
    }
}
