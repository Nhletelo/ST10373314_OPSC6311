package com.example.CoinWatch

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        // Load settings into the FrameLayout
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.settings_container, SettingsFragment())
            }
        }

        // Back button functionality
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}
