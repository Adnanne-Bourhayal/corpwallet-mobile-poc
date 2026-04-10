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

class PendingTransactionsActivity : AppCompatActivity() {

    private lateinit var tvQueueSummaryValue: TextView
    private lateinit var containerTransactions: LinearLayout

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#05070B")
        window.navigationBarColor = Color.parseColor("#05070B")
        setContentView(R.layout.activity_pending_transactions)

        tvQueueSummaryValue = findViewById(R.id.tvQueueSummaryValue)
        containerTransactions = findViewById(R.id.containerTransactions)

        loadPendingTransactions()
    }

    override fun onResume() {
        super.onResume()
        loadPendingTransactions()
    }

    private fun loadPendingTransactions() {
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
                            tvQueueSummaryValue.text = "Queue unavailable"
                        }
                    }
                }
            } catch (e: IOException) {
                runOnUiThread {
                    tvQueueSummaryValue.text = "Connection unavailable"
                }
            }
        }
    }

    private fun renderTransactions(json: String) {
        try {
            val array = JSONArray(json)
            tvQueueSummaryValue.text = "${array.length()} approvals pending"
            containerTransactions.removeAllViews()

            if (array.length() == 0) {
                val emptyText = TextView(this).apply {
                    text = "No pending approvals."
                    textSize = 16f
                    setTextColor(android.graphics.Color.parseColor("#C7D1E5"))
                }
                containerTransactions.addView(emptyText)
                return
            }

            val inflater = LayoutInflater.from(this)

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)

                val card = inflater.inflate(
                    R.layout.item_transaction_card,
                    containerTransactions,
                    false
                )

                val tvTxId = card.findViewById<TextView>(R.id.tvTxId)
                val tvRecipient = card.findViewById<TextView>(R.id.tvRecipient)
                val tvStatus = card.findViewById<TextView>(R.id.tvStatus)
                val tvAmount = card.findViewById<TextView>(R.id.tvAmount)
                val tvRisk = card.findViewById<TextView>(R.id.tvRisk)
                val tvMeta = card.findViewById<TextView>(R.id.tvMeta)

                val id = item.getInt("id")
                val recipient = item.getString("recipient")
                val amount = item.getDouble("amount")
                val risk = item.getInt("risk_score")
                val status = item.getString("status")

                tvTxId.text = "TX-$id"
                tvRecipient.text = recipient
                tvStatus.text = status.replaceFirstChar { it.uppercase() }
                tvAmount.text = "€${"%.2f".format(amount)}"
                tvRisk.text = "Risk Score $risk"

                tvMeta.text = if (risk >= 70) {
                    "Elevated review priority"
                } else {
                    "Standard trusted review"
                }

                card.setOnClickListener {
                    val intent = Intent(this, TransactionDetailActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("recipient", recipient)
                    intent.putExtra("amount", amount)
                    intent.putExtra("risk", risk)
                    intent.putExtra("status", status)
                    startActivity(intent)
                }

                containerTransactions.addView(card)
            }

        } catch (e: Exception) {
            tvQueueSummaryValue.text = "Parsing failed"
        }
    }
}
