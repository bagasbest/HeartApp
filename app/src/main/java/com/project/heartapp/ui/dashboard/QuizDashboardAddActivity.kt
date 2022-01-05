package com.project.heartapp.ui.dashboard

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.project.heartapp.R
import com.project.heartapp.databinding.ActivityQuizDashboardAddBinding

import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore


class QuizDashboardAddActivity : AppCompatActivity() {

    private var binding: ActivityQuizDashboardAddBinding? = null
    private var time: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizDashboardAddBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        // akan tampil spinner untuk memasukkan waktu quiz set, contoh 10 menit, 15 Menit, 20 Menit
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.time, android.R.layout.simple_list_item_1
        )
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        binding?.time?.setAdapter(adapter)
        binding?.time?.setOnItemClickListener { _, _, _, _ ->
            time = binding?.time?.text.toString()
        }


        /// ketika user klik tombol kembali
        binding?.back?.setOnClickListener {
            onBackPressed()
        }

        /// ketika user klik save, maka akan ada validasi sebelum menyimpan data di database
        binding?.save?.setOnClickListener {
            formValidate()
        }
    }


    /// ketika user klik save, maka akan ada validasi sebelum menyimpan data di database
    private fun formValidate() {
        val title = binding?.title?.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Title must be filled", Toast.LENGTH_SHORT).show()
            return
        } else if (time == null) {
            Toast.makeText(this, "Time must be filled", Toast.LENGTH_SHORT).show()
            return
        }


        binding?.progressBar?.visibility = View.VISIBLE
        val quizId = System.currentTimeMillis().toString()
        val questionMap: MutableMap<String, Any> = HashMap()
        questionMap["quizId"] = quizId
        questionMap["title"] = title
        when (time) {
            /// 5*60*1000 itu representasi dari 5 menit, 5*60*1000 itu merupakan format mil detik yang digunakan pada date format
            /// karena date format hanya menerima format berupa waktu dalam mil detik, oleh karena itu 5 menit disini di representasikan dalam mildetik yaitu 5*60*1000
            "5 Minutes" -> {
                questionMap["time"] = 5 * 60 * 1000.toLong()
            }
            "10 Minutes" -> {
                questionMap["time"] = 10 * 60 * 1000.toLong()
            }
            "15 Minutes" -> {
                questionMap["time"] = 15 * 60 * 1000.toLong()
            }
            "20 Minutes" -> {
                questionMap["time"] = 20 * 60 * 1000.toLong()
            }
            "25 Minutes" -> {
                questionMap["time"] = 25 * 60 * 1000.toLong()
            }
            "30 Minutes" -> {
                questionMap["time"] = 30 * 60 * 1000.toLong()
            }
            "35 Minutes" -> {
                questionMap["time"] = 35 * 60 * 1000.toLong()
            }
            "40 Minutes" -> {
                questionMap["time"] = 40 * 60 * 1000.toLong()
            }
            "45 Minutes" -> {
                questionMap["time"] = 45 * 60 * 1000.toLong()
            }
            "50 Minutes" -> {
                questionMap["time"] = 50 * 60 * 1000.toLong()
            }
            "55 Minutes" -> {
                questionMap["time"] = 55 * 60 * 1000.toLong()
            }
            "60 Minutes" -> {
                questionMap["time"] = 60 * 60 * 1000.toLong()
            }
        }


        /// simpan quiz set baru kedalam database
        FirebaseFirestore
            .getInstance()
            .collection("quiz_dashboard")
            .document(quizId)
            .set(questionMap)
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    binding?.progressBar?.visibility = View.GONE
                    showSuccessDialog()
                } else {
                    binding?.progressBar?.visibility = View.GONE
                    showFailureDialog()
                }
            }
    }

    private fun showFailureDialog() {
        AlertDialog.Builder(this)
            .setTitle("Failure Create Quiz")
            .setMessage("Please check your internet connection, and try again later")
            .setIcon(R.drawable.ic_baseline_clear_24)
            .setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success Create Quiz")
            .setMessage("Quiz will be add soon")
            .setIcon(R.drawable.ic_baseline_check_circle_outline_24)
            .setPositiveButton("OK") { dialogInterface, _ ->
                dialogInterface.dismiss()
                onBackPressed()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}