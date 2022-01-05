package com.project.heartapp.ui.dashboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.heartapp.databinding.ActivityQuizDashboardBinding

class QuizDashboardActivity : AppCompatActivity() {

    /// variabel yang diinisiasi secara global berfungsi untuk membantu program secara efisien
    private var binding: ActivityQuizDashboardBinding? = null
    private var adapter: QuizDashboardAdapter? = null
    private var role: String? = null

    /// mula mula kita check role pengguna dulu, apakah ia merupakan admin atau user
    override fun onResume() {
        super.onResume()
        checkRole()
    }

    private fun checkRole() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {
                /// jika role adalah admin, maka akan ada tombol + untuk membuat quiz set baru, dan menghapus quiz set yang ada,
                /// jika role adalah user biasa, maka tidak ada tombol + dan ikon delete pada quiz set
                role = it.data?.get("role").toString()
                if(role == "admin") {
                    binding?.addQuiz?.visibility = View.VISIBLE
                }

                /// setelah itu sistem akan menampilkan quiz set yang sudah di buat sebelumnya, ditampilkan dalam bentuk list
                initRecyclerView()
                initViewModel()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizDashboardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        /// ketika role adalah admin, dan ia menekan ikon + , maka ia akan diarahkan ke halaman add quiz set
        binding?.addQuiz?.setOnClickListener {
            startActivity(Intent(this, QuizDashboardAddActivity::class.java))
        }
    }

    /// fungsi untuk menampilkan quiz set yang berhasil didapatkan dari database
    private fun initRecyclerView() {
        binding?.rvQuiz?.layoutManager = LinearLayoutManager(this)
        adapter = QuizDashboardAdapter(role)
        binding?.rvQuiz?.adapter = adapter
    }

    /// fungsi untuk mendapatkan quiz set dari firebase
    /// melalui kelas QuizDashboardViewModel, kemudian di transfer ke kelas QuizDashboardActivity (kelas ini)
    /// kemudian di cek, apakah ada quiz yang dibuat sebelumnhya atau tidak
    /// jika ada, maka initRecyclerView akan menampilkan data quiz set
    /// jika tidak ada maka akan muncul tulisan Nothing Quiz Set Available
    private fun initViewModel() {
        val viewModel = ViewModelProvider(this)[QuizDashboardViewModel::class.java]

        binding?.progressBar?.visibility = View.VISIBLE
        viewModel.setListQuiz()
        viewModel.getQuizList().observe(this) { quiz ->
            if (quiz.size > 0) {
                adapter!!.setData(quiz)
                binding?.noData?.visibility = View.GONE
            } else {
                binding?.noData?.visibility = View.VISIBLE
            }
            binding!!.progressBar.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}