package com.sophi.sample

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sophi.android.SophiChatFragment
import com.sophi.sample.databinding.ActivityEmbeddedBinding

class EmbeddedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmbeddedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEmbeddedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // The chat lifts itself above the keyboard; the root only keeps it off the navigation bar.
            val keyboardOpen = insets.isVisible(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, if (keyboardOpen) 0 else systemBars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        // Closing arrives as a fragment result, so it survives rotation and process death.
        supportFragmentManager.setFragmentResultListener(SophiChatFragment.REQUEST_KEY_CLOSE, this) { _, _ -> finish() }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.sophiContainer.id, SophiChatFragment.newInstance())
                .commit()
        }
    }
}
