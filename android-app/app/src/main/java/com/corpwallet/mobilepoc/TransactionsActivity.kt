package com.corpwallet.mobilepoc

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException
import kotlin.concurrent.thread

class TransactionsActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    private lateinit var btnTransactionsBack: TextView
    private lateinit var containerTransactionsList: LinearLayout
    private lateinit var tvTransactionsCount: TextView
    private lateinit var tvApprovedMetric: TextView
    private lateinit var tvRiskMetric: TextView

    private lateinit var navDashboard: LinearLayout
    private lateinit var navTransactions: LinearLayout
    private lateinit var navSecurity: LinearLayout
    private lateinit var navSettings: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#040B18")
        window.navigationBarColor = Color.parseColor("#040B18")
        setContentView(R.layout.activity_transactions)

        btnTransactionsBack = findViewById(R.id.btnTransactionsBack)
        containerTransactionsList = findViewById(R.id.containerTransactionsList)
        tvTransactionsCount = findViewById(R.id.tvTransactionsCount)
        tvApprovedMetric = findViewById(R.id.tvApprovedMetric)
        tvRiskMetric = findViewById(R.id.tvRiskMetric)

        navDashboard = findViewById(R.id.navDashboard)
        navTransactions = findViewById(R.id.navTransactions)
        navSecurity = findViewById(R.id.navSecurity)
        navSettings = findViewById(R.id.navSettings)

        btnTransactionsBack.setOnClickListener { finish() }

        navDashboard.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        navSecurity.setOnClickListener {
            startActivity(Intent(this, SecurityActivity::class.java))
        }

        navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        loadTransactions()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    private fun loadTransactions() {
        thread {
            val url = "http://192.168.0.192:8000/transactions/pending"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: "[]"

                    runOnUiThread {
                        if (response.isSuccessful) {
                            renderTransactions(body)
                        } else {
                            tvTransactionsCount.text = "Queue unavailable"
                        }
                    }
                }
            } catch (e: IOException) {
                runOnUiThread {
                    tvTransactionsCount.text = "Connection unavailable"
                }
            }
        }
    }

    private fun renderTransactions(json: String) {
        try {
            val array = JSONArray(json)
            containerTransactionsList.removeAllViews()

            val count = array.length()
            tvTransactionsCount.text = "$count Pending"

            var highRisk = 0

            if (count == 0) {
                val empty = TextView(this).apply {
                    text = "No pending approvals."
                    textSize = 16f
                    setTextColor(Color.parseColor("#C7D1E5"))
                }
                containerTransactionsList.addView(empty)
                tvApprovedMetric.text = "Approved 100%"
                tvRiskMetric.text = "High Risk 0"
                return
            }

            val inflater = LayoutInflater.from(this)

            for (i in 0 until count) {
                val item = array.getJSONObject(i)
                val id = item.getInt("id")
                val recipient = item.getString("recipient")
                val amount = item.getDouble("amount")
                val risk = item.getInt("risk_score")
                val status = item.getString("status")

                if (risk >= 70) highRisk++

                val card = inflater.inflate(
                    R.layout.item_transaction_modern,
                    containerTransactionsList,
                    false
                )

                val tvRecipient = card.findViewById<TextView>(R.id.tvRecipient)
                val tvTxId = card.findViewById<TextView>(R.id.tvTxId)
                val tvRiskBadge = card.findViewById<TextView>(R.id.tvRiskBadge)
                val tvStatusBadge = card.findViewById<TextView>(R.id.tvStatusBadge)
                val tvAmount = card.findViewById<TextView>(R.id.tvAmount)
                val tvDay = card.findViewById<TextView>(R.id.tvDay)

                tvRecipient.text = recipient
                tvTxId.text = "TX-$id"
                tvRiskBadge.text = "Risk $risk"
                tvStatusBadge.text = status.replaceFirstChar { it.uppercase() }
                tvAmount.text = "€${"%.2f".format(amount)}"
                tvDay.text = "Today"

                card.setOnClickListener {
                    val intent = Intent(this, TransactionDetailActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("recipient", recipient)
                    intent.putExtra("amount", amount)
                    intent.putExtra("risk", risk)
                    intent.putExtra("status", status)
                    startActivity(intent)
                }

                containerTransactionsList.addView(card)
            }

            tvApprovedMetric.text = "Approved 96%"
            tvRiskMetric.text = "High Risk $highRisk"

        } catch (e: Exception) {
            tvTransactionsCount.text = "Parsing failed"
        }
    }
}
