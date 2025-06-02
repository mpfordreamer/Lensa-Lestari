package com.example.lensalestari.ui.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.lensalestari.R
import com.example.lensalestari.databinding.ActivityMainBinding
import com.example.lensalestari.ui.home.HomeFragment
import com.example.lensalestari.ui.reward.RewardFragment
import com.example.lensalestari.ui.scan.ScanFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment(HomeFragment())

        binding.btnDashboard.setOnClickListener {
            loadFragment(HomeFragment())
        }
        binding.btnProfile.setOnClickListener {
            loadFragment(RewardFragment())
        }
        binding.fabScan.setOnClickListener {
            loadFragment(ScanFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}