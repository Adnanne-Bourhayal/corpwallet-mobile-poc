package com.corpwallet.mobilepoc

import android.content.Context
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import org.json.JSONObject

/** Collect a fictional request. The API decides whether a destination is eligible. */
internal fun showExpenseForm(context: Context, submit: (JSONObject) -> Unit) {
    val form = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        val pad = (20 * resources.displayMetrics.density).toInt()
        setPadding(pad, 0, pad, 0)
    }
    fun field(label: String): EditText = EditText(context).also {
        it.hint = label
        it.isSingleLine = true
        form.addView(it)
    }
    val payee = field("Example supplier")
    val purpose = field("Team expense purpose")
    val amount = field("Amount in euros").apply {
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }
    form.addView(TextView(context).apply { text = "Payment destination" })
    val destination = Spinner(context)
    destination.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listOf("Supplier invoice", "Crypto wallet"))
    form.addView(destination)
    val dialog = AlertDialog.Builder(context).setTitle("New sample expense")
        .setView(ScrollView(context).apply { addView(form) })
        .setNegativeButton("Cancel", null).setPositiveButton("Submit", null).create()
    dialog.show()
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        val recipient = payee.text.toString().trim()
        val reason = purpose.text.toString().trim()
        val euros = amount.text.toString().replace(',', '.').toBigDecimalOrNull()
        when {
            recipient.length !in 1..80 -> payee.error = "Use 1–80 characters."
            reason.length !in 1..160 -> purpose.error = "Use 1–160 characters."
            euros == null || euros.signum() <= 0 || euros.scale() > 2 || euros.toDouble() > 1_000_000 -> amount.error = "Enter a positive amount up to €1,000,000 with up to two decimal places."
            else -> {
                val type = if (destination.selectedItemPosition == 1) "crypto_wallet" else "supplier_invoice"
                submit(JSONObject().put("recipient", recipient).put("purpose", reason).put("amount", euros.toDouble()).put("destination", type))
                dialog.dismiss()
            }
        }
    }
}
