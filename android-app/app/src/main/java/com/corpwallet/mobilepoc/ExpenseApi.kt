package com.corpwallet.mobilepoc

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

internal data class Expense(
    val id: Int, val recipient: String, val purpose: String, val amount: Double,
    val currency: String, val destination: String, val status: String, val reason: String
) {
    companion object {
        fun fromJson(row: JSONObject) = Expense(
            row.getInt("id"), row.getString("recipient"), row.getString("purpose"),
            row.getDouble("amount"), row.getString("currency"), row.getString("destination"),
            row.getString("status"), row.getString("decision_reason")
        )
    }
}

/** Run requests off the UI thread. Only the API can change a decision. */
internal class ExpenseApi {
    private val client = OkHttpClient.Builder().followRedirects(false).build()

    private fun request(path: String, post: Boolean = false, payload: JSONObject? = null): String {
        check(BuildConfig.API_BASE.isNotEmpty()) { "Build the release with your HTTPS API address." }
        val request = Request.Builder().url(BuildConfig.API_BASE + path)
        if (post) request.post(payload?.toString()?.toRequestBody("application/json".toMediaType()) ?: ByteArray(0).toRequestBody(null))
        client.newCall(request.build()).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val explanation = runCatching { JSONObject(body).getString("detail") }.getOrNull()
                throw IOException(explanation ?: "Could not complete the request.")
            }
            return body
        }
    }

    fun list(): List<Expense> {
        val rows = JSONArray(request("/transactions"))
        return (0 until rows.length()).map { Expense.fromJson(rows.getJSONObject(it)) }
    }

    fun submit(proposal: JSONObject) {
        request("/transactions", post = true, payload = proposal)
    }

    fun decide(id: Int, approve: Boolean) {
        val action = if (approve) "approve" else "reject"
        request("/transactions/$id/$action", post = true)
    }
}
