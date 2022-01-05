package com.project.heartapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.project.heartapp.auth.LoginActivity
import com.project.heartapp.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    /// inisiasi variabel supaya variabel ini bisa diakses secara global
    private var binding: ActivityMainBinding? = null

    /// fungsung untuk menampilkan splash screen Heart app selama 2 detik, kemudian masuk ke halaman login
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        Timer().schedule(object : TimerTask() {
            override fun run() {

                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
        }, 2000)


    }

    /// fungsi untuk menghancurkan activity jika activity ini sudah tidak digunakan, agar menghindari kebocoran memory pada aplikasi, yang menghindari freeze screen
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}