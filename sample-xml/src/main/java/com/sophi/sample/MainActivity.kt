package com.sophi.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sophi.android.Sophi
import com.sophi.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.buttonStart.setOnClickListener { Sophi.start(this) }
        binding.buttonStartMessage.setOnClickListener { Sophi.start(this, initialMessage = DEMO_MESSAGE) }
        binding.buttonStartProduct.setOnClickListener { Sophi.start(this, externalProductId = DEMO_PRODUCT_ID) }
        binding.buttonEmbedded.setOnClickListener { startActivity(Intent(this, EmbeddedActivity::class.java)) }
    }

    private companion object {
        const val DEMO_MESSAGE = "Merhaba, bir öneri alabilir miyim?"
        // externalId of a product in the Dev demo catalog; use one of your own catalog's ids.
        const val DEMO_PRODUCT_ID = "T25003337ME0"
    }
}
