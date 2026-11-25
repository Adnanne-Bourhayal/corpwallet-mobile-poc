package com.corpwallet.mobilepoc

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONObject
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import java.util.concurrent.Executors

/** Local expense desk. The review log is fetched again after every decision. */
class PendingTransactionsActivity : AppCompatActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val api = ExpenseApi()
    private lateinit var summary: TextView
    private lateinit var list: LinearLayout
    private lateinit var refresh: ImageButton
    private lateinit var pendingAmount: TextView
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
            }.onSuccess { Toast.makeText(applicationContext, "Evidence saved", Toast.LENGTH_SHORT).show() }
             .onFailure { Toast.makeText(applicationContext, "Could not save evidence", Toast.LENGTH_LONG).show() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_transactions)
        summary = findViewById(R.id.tvQueueSummaryValue)
        pendingAmount = findViewById(R.id.tvPendingAmount)
        list = findViewById(R.id.containerTransactions)
        refresh = findViewById(R.id.refresh)
        findViewById<Button>(R.id.newExpense).setOnClickListener { showExpenseForm(this) { proposal -> update { api.submit(proposal) } } }
        refresh.setOnClickListener { update() }
        val report = findViewById<ImageButton>(R.id.export)
        TooltipCompat.setTooltipText(report, report.contentDescription)
        report.setOnClickListener { export.launch("corpwallet-evidence.json") }
        update()
    }

    private fun update(action: (() -> Unit)? = null) {
        refresh.isEnabled = false
        list.removeAllViews()
        pendingAmount.text = "—"
        summary.text = "Updating expenses…"
        executor.execute {
            val result = runCatching { action?.invoke(); api.list() }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                refresh.isEnabled = true
                result.onSuccess { rows -> expenses = rows; render() }
                    .onFailure {
                        summary.text = when {
                            BuildConfig.API_BASE.isEmpty() -> "Set an HTTPS API address for this build."
                            else -> it.localizedMessage ?: "Could not load expenses. Tap Refresh to retry."
                        }
                    }
            }
        }
    }

    private fun render() {
        // The API accepts EUR only. Sum decimal values before formatting the pending total.
        list.removeAllViews()
        val pending = expenses.filter { it.status == "pending" }
        val total = pending.fold(BigDecimal.ZERO) { amount, request -> amount + BigDecimal.valueOf(request.amount) }
        pendingAmount.text = currencyFormat("EUR").format(total)
        summary.text = when {
            expenses.isEmpty() -> "No requests yet"
            pending.isEmpty() -> "All requests reviewed"
            pending.size == 1 -> "1 request to review"
            else -> "${pending.size} requests to review"
        }
        expenses.forEach { row ->
            val card = layoutInflater.inflate(R.layout.item_transaction_card, list, false) as LinearLayout
            val amount = currencyFormat(row.currency).format(row.amount)
            // At large font sizes or on narrow phones a stacked row gives both columns their full width.
            val configuration = resources.configuration
            if (configuration.screenWidthDp < 360 || configuration.fontScale > 1.25f) {
                card.orientation = LinearLayout.VERTICAL
                card.gravity = Gravity.START
                card.findViewById<TextView>(R.id.tvMonogram).visibility = View.GONE
                card.findViewById<LinearLayout>(R.id.recipientColumn).layoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                card.findViewById<LinearLayout>(R.id.valueColumn).apply {
                    gravity = Gravity.START
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
            }
            val initials = row.recipient.split(Regex("\\s+")).filter { it.isNotEmpty() && it.first().isLetterOrDigit() }
                .take(2).joinToString("") { it.take(1) }.uppercase(Locale.getDefault())
            card.findViewById<TextView>(R.id.tvMonogram).text = initials.ifEmpty { "•" }
            card.findViewById<TextView>(R.id.tvRecipient).text = row.recipient
            card.findViewById<TextView>(R.id.tvAmount).text = amount
            card.findViewById<TextView>(R.id.tvPurpose).text = row.purpose
            labelStatus(card.findViewById(R.id.tvStatus), row.status)
            card.setOnClickListener { inspect(row, amount) }
            list.addView(card)
        }
    }

    private fun currencyFormat(code: String) = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance(code)
    }

    private fun labelStatus(view: TextView, status: String) {
        val (label, colour) = when (status) {
            "approved" -> "Approved" to R.color.approved
            "rejected" -> "Declined" to R.color.declined
            "pending" -> "Pending" to R.color.waiting
            else -> "Unknown status" to R.color.muted
        }
        view.text = label
        view.setTextColor(getColor(colour))
    }

    private fun inspect(row: Expense, amount: String) {
        val dialog = BottomSheetDialog(this)
        val details = layoutInflater.inflate(R.layout.expense_details, null)
        details.findViewById<TextView>(R.id.detailSupplier).text = row.recipient
        details.findViewById<ImageButton>(R.id.detailClose).setOnClickListener { dialog.dismiss() }
        details.findViewById<TextView>(R.id.detailAmount).text = amount
        labelStatus(details.findViewById(R.id.detailStatus), row.status)
        details.findViewById<TextView>(R.id.detailPurpose).text = row.purpose
        val destination = if (row.destination == "crypto_wallet") "Crypto wallet" else "Supplier invoice"
        details.findViewById<TextView>(R.id.detailDestination).text = "Request #${row.id} · $destination"
        details.findViewById<TextView>(R.id.detailReason).apply {
            text = row.reason
            visibility = if (row.reason.isEmpty()) View.GONE else View.VISIBLE
        }
        val actions = details.findViewById<LinearLayout>(R.id.detailActions)
        if (row.status == "pending") {
            details.findViewById<Button>(R.id.detailApprove).setOnClickListener {
                dialog.dismiss()
                update { api.decide(row.id, true) }
            }
            details.findViewById<Button>(R.id.detailDecline).setOnClickListener {
                dialog.dismiss()
                update { api.decide(row.id, false) }
            }
        } else {
            actions.visibility = View.GONE
        }
        dialog.setContentView(details)
        dialog.behavior.skipCollapsed = true
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }
}
