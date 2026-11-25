package com.corpwallet.mobilepoc

import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

/** Collect a fictional request. The API decides whether a destination is eligible. */
internal fun showExpenseForm(context: Context, submit: (JSONObject) -> Unit) {
    val builder = MaterialAlertDialogBuilder(context).setTitle("New expense")
    val form = LayoutInflater.from(builder.context).inflate(R.layout.expense_form, null)
    val payee = form.findViewById<EditText>(R.id.formSupplier)
    val purpose = form.findViewById<EditText>(R.id.formPurpose)
    val amount = form.findViewById<EditText>(R.id.formAmount)
    val destination = form.findViewById<Spinner>(R.id.formDestination)
    destination.adapter = ArrayAdapter(builder.context, android.R.layout.simple_spinner_dropdown_item, listOf("Supplier invoice", "Crypto wallet"))
    val dialog = builder.setView(form)
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
