package com.sophi.sample

import android.os.Bundle
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sophi.android.Sophi
import com.sophi.sample.databinding.ActivityMainBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val host = application as SampleApplication
        if (intent.getBooleanExtra("sophi_local_demo", false) && !host.localDemo) host.configure(true)
        lifecycleScope.launch { host.ready.collect { ready ->
            binding.status.text = if (ready) (if (host.localDemo) "Local chat + events\nSynthetic data; no real orders." else "M&S · Development environment") else "Preparing chat + events…"
            binding.fabSophi.isEnabled = ready; binding.events.isEnabled = ready
        } }
        binding.events.setOnClickListener { startActivity(Intent(this, DemoEventsActivity::class.java)) }
        binding.local.setOnClickListener { host.configure(true) }
        binding.development.setOnClickListener { host.configure(false) }
        binding.apply {
            fabSophi.setOnClickListener {
                Sophi.start(this@MainActivity)
            }
        }
    }
}
