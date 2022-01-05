package com.project.heartapp.ui.about

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.project.heartapp.R
import com.project.heartapp.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private var binding: ActivityAboutBinding? = null


    /// menampilkan halaman about
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding?.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}