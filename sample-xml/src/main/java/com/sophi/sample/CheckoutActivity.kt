package com.sophi.sample

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/** Merchant-owned checkout destination. This sample displays intent only; it has no order API. */
class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
        content.addView(Button(this).apply { text = "Complete demo order"; isEnabled = MerchantCart.lines.value.isNotEmpty()
            setOnClickListener { if (DemoTracking.placeDemoOrder()) { isEnabled = false; finish() } }
        })
        val scroll = ScrollView(this).apply { addView(content) }
        setContentView(scroll)
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom); insets
        }
    }
}
