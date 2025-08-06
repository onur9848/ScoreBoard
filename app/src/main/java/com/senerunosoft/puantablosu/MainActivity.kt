package com.senerunosoft.puantablosu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.senerunosoft.puantablosu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}