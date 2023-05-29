package com.fazziclay.opentoday.gui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fazziclay.opentoday.databinding.ActivityChangelogBinding

class ChangelogActivity : AppCompatActivity() {
    companion object {
        fun createLaunchIntent(requireContext: Context): Intent {
            return Intent(requireContext, ChangelogActivity::class.java)
        }
    }

    private lateinit var binding: ActivityChangelogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangelogBinding.inflate(layoutInflater)
        try {
            binding.changelogText.text = OpenSourceLicenseActivity.read(assets.open("CHANGELOG"))
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setContentView(binding.root)
    }
}
