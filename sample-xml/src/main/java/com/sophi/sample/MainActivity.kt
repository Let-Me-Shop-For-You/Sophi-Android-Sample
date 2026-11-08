package com.sophi.sample

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val configured = (application as SampleApplication).configured
        binding.status.text = if (configured) "M&S · Development environment\nCart changes stay in this demo app." else "Add a development MNS_API_KEY to local.properties, then rebuild."
        binding.fabSophi.isEnabled = configured
        binding.apply {
            fabSophi.setOnClickListener {
                Sophi.start(this@MainActivity)
            }
        }
    }
}