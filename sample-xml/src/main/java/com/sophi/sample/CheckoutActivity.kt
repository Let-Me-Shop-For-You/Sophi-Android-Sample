package com.sophi.sample

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** Merchant-owned checkout destination. This sample displays intent only; it has no order API. */
class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (24 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }
        content.addView(TextView(this).apply {
            text = "M&S demo cart\n\nLocal cart only — no order or payment is created.\n\n" +
                MerchantCart.lines.value.joinToString("\n") { "${it.label}  ${it.lineTotal}" }.ifBlank { "Your cart is empty." }
            textSize = 18f
        })
        content.addView(Button(this).apply { text = "Back"; setOnClickListener { finish() } })
        setContentView(ScrollView(this).apply { addView(content) })
    }
}
