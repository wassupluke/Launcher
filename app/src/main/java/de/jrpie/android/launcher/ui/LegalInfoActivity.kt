package de.jrpie.android.launcher.ui

import android.content.res.Resources
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.databinding.LegalInfoBinding

class LegalInfoActivity : AppCompatActivity(), UIObject {
    private lateinit var binding: LegalInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()

        // Initialise layout
        binding = LegalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTitle(R.string.legal_info_title)
        setSupportActionBar(binding.legalInfoAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStart() {
        super<AppCompatActivity>.onStart()
        super<UIObject>.onStart()
    }

    override fun getTheme(): Resources.Theme {
        return modifyTheme(super.getTheme())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}