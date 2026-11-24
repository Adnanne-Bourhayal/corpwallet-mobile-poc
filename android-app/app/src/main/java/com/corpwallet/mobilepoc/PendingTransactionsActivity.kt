package com.corpwallet.mobilepoc

import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Currency
import java.util.concurrent.Executors

/** Local expense desk. The review log is fetched again after every decision. */
class PendingTransactionsActivity : AppCompatActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val api = ExpenseApi()
    private lateinit var summary: TextView
    private lateinit var list: LinearLayout
    private lateinit var refresh: Button
    private var expenses: List<Expense> = emptyList()

    // Android's document picker lets the reviewer choose where the evidence is saved.
    private val export = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            val signals = JSONObject()
                .put("debug", applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)
                .put("obfuscation", BuildConfig.R8_ENABLED)
            val evidence = JSONObject().put("name", "CorpWallet").put("source", "Android build settings")
                .put("schemaVersion", 1).put("signals", signals)
            runCatching {
                requireNotNull(contentResolver.openOutputStream(uri)).bufferedWriter().use { it.write(evidence.toString(2)) }
            }.onSuccess { Toast.makeText(this, "Evidence saved", Toast.LENGTH_SHORT).show() }
             .onFailure { Toast.makeText(this, "Could not save evidence", Toast.LENGTH_LONG).show() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_transactions)
        summary = findViewById(R.id.tvQueueSummaryValue)
        list = findViewById(R.id.containerTransactions)
        refresh = findViewById(R.id.refresh)
        findViewById<Button>(R.id.newExpense).setOnClickListener { showExpenseForm(this) { proposal -> update { api.submit(proposal) } } }
        refresh.setOnClickListener { update() }
        findViewById<Button>(R.id.export).setOnClickListener { export.launch("corpwallet-evidence.json") }
        update()
    }

    private fun update(action: (() -> Unit)? = null) {
        refresh.isEnabled = false
        list.removeAllViews()
        summary.text = "Updating expenses…"
        executor.execute {
            val result = runCatching { action?.invoke(); api.list() }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                refresh.isEnabled = true
                result.onSuccess { rows -> expenses = rows; render() }
                    .onFailure {
                        summary.text = if (BuildConfig.API_BASE.isEmpty()) "Set an HTTPS API address for this build."
                        else "Review unavailable · tap Refresh"
                        Toast.makeText(this, it.localizedMessage ?: "Connection unavailable", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun render() {
        val pending = expenses.count { it.status == "pending" }
        summary.text = if (pending == 1) "1 expense to review" else "$pending expenses to review"
        expenses.forEach { row ->
            val card = layoutInflater.inflate(R.layout.item_transaction_card, list, false)
            val amount = NumberFormat.getCurrencyInstance().apply { currency = Currency.getInstance(row.currency) }.format(row.amount)
            card.findViewById<TextView>(R.id.tvRecipient).text = row.recipient
            card.findViewById<TextView>(R.id.tvTxId).text = "EXPENSE " + row.id.toString().padStart(2, '0')
            card.findViewById<TextView>(R.id.tvAmount).text = amount
            card.findViewById<TextView>(R.id.tvRisk).text = row.purpose
            card.findViewById<TextView>(R.id.tvMeta).text = if (row.reason.isEmpty()) "Supplier invoice" else row.reason
            val status = card.findViewById<TextView>(R.id.tvStatus)
            status.text = when (row.status) { "approved" -> "Approved"; "rejected" -> "Declined"; else -> "Review" }
            status.setTextColor(Color.parseColor(if (row.status == "rejected") "#FFB7AA" else "#C9E4FF"))
            card.isFocusable = true
            card.setOnClickListener { inspect(row, amount) }
            list.addView(card)
        }
    }

    private fun inspect(row: Expense, amount: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(row.recipient)
            .setMessage(listOf(amount, row.purpose, row.reason).filter { it.isNotEmpty() }.joinToString("\n\n"))
            .setNeutralButton("Close", null)
        if (row.status == "pending") {
            dialog.setPositiveButton("Approve") { _, _ -> update { api.decide(row.id, true) } }
            dialog.setNegativeButton("Decline") { _, _ -> update { api.decide(row.id, false) } }
        }
        dialog.show()
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }
}
