package com.project.heartapp.ui

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.project.heartapp.R
import com.project.heartapp.auth.LoginActivity
import com.project.heartapp.databinding.ActivityHomepageBinding
import com.project.heartapp.ui.about.AboutActivity
import com.project.heartapp.ui.dashboard.QuizDashboardActivity
import com.project.heartapp.ui.quiz.QuizActivity

class HomepageActivity : AppCompatActivity() {

    private var binding: ActivityHomepageBinding? = null

    //// home page ada 5 tombol, yang berfungsi hanya mini quiz, logout, dan about us
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        //// ketika user klik mini quiz, maka akan mengarah ke halaman Dashboard Quiz Set
        binding?.quiz?.setOnClickListener {
            startActivity(Intent(this, QuizDashboardActivity::class.java))
        }

        /// ketika user klik about app, maka akan mengarah kehalaman about
        binding?.about?.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }


        /// ketika user klik exit, maka akan muncul alert dialog konfirmasi logout, untuk memastikan apakah user ingin logout atau tidak
        binding?.exit?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Exit Confirmation")
                .setMessage("Are you sure want to exit ?")
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setPositiveButton("YES") { dialogInterface, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    dialogInterface.dismiss()
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("NO", null)
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }


}