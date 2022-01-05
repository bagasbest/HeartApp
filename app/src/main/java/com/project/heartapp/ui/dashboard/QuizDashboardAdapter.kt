package com.project.heartapp.ui.dashboard

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.project.heartapp.R
import com.project.heartapp.databinding.ItemQuizBinding
import com.project.heartapp.ui.quiz.QuizActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/// kelas adapter membantu kelas QuizDashboardActivity dalam menampilkan data quiz set dalam betuk list
class QuizDashboardAdapter(private val role: String?) :
    RecyclerView.Adapter<QuizDashboardAdapter.ViewHolder>() {


    ///pertama sistem akan menampung data quiz set yang sudah didapatkan dari database
    /// ditampung dalam arraylist (quizList) -> berisi data quiz set
    private val quizList = ArrayList<QuizDashboardModel>()

    @SuppressLint("SimpleDateFormat")
    /// simple date format berfungsi untuk menampilkan waktu quiz nya: misal 10:00 menit
    private val formatter = SimpleDateFormat("mm:ss")

    @SuppressLint("NotifyDataSetChanged")
    fun setData(items: ArrayList<QuizDashboardModel>) {
        quizList.clear()
        quizList.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemQuizBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
        fun bind(model: QuizDashboardModel) {
            with(binding) {

                /// sistem memunculkan judul quiz set
                title.text = model.title
                /// sistem memunculkan waktu quiz set
                val date = Date(model.time)
                if (model.time < 3600000) {
                    time.text = "Waktu: ${formatter.format(date)} Menit"
                } else {
                    time.text = "Waktu: 60 Menit"
                }

                /// jika role adalah admin, maka akan muncul ikon delete quiz set
                if (role == "admin") {
                    deleteQuiz.visibility = View.VISIBLE
                }

                /// jika item quiz set di klik, maka user akan memulai quiz
                cv.setOnClickListener {
                    val intent = Intent(itemView.context, QuizActivity::class.java)
                    intent.putExtra(QuizActivity.EXTRA_QUIZ, model)
                    intent.putExtra(QuizActivity.TIME, model.time)
                    itemView.context.startActivity(intent)
                }


                /// jika ikon delete quiz ditekan, maka akan menghapus quiz set berdasarkan item dimana ia ditekan
                binding.deleteQuiz.setOnClickListener {

                    AlertDialog.Builder(itemView.context)
                        .setTitle("Confirm Delete Quiz Set")
                        .setMessage("Are you sure want to delete this Quiz Set ?")
                        .setIcon(R.drawable.ic_baseline_warning_24)
                        .setPositiveButton("YES") { dialog, _ ->

                            dialog.dismiss()
                            val mProgressDialog = ProgressDialog(itemView.context)
                            mProgressDialog.setMessage("Please wait until process finish...")
                            mProgressDialog.setCanceledOnTouchOutside(false)
                            mProgressDialog.show()

                            /// menghapus quiz set dari database
                            model.quizId?.let { it1 ->
                                FirebaseFirestore
                                    .getInstance()
                                    .collection("quiz_dashboard")
                                    .document(it1)
                                    .delete()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {


                                            /// jika sukses menghapus quiz set, hapus juga pertanyaan pertanyaan didalamnya
                                            /// karena tiap quiz set berisi beberapa pertanyaan
                                            val listOfQuiz = ArrayList<String>()
                                            FirebaseFirestore
                                                .getInstance()
                                                .collection("quiz")
                                                .whereEqualTo("quizId", model.quizId)
                                                .get()
                                                .addOnSuccessListener { documents ->

                                                    /// proses menghapus pertanyaan pertanyaan yang ada di quiz set
                                                    /// sistem akan mencari tahu seberapa banyak pertanyaan yang akan di hapus
                                                    /// kemudian sistem akan menghapus pertanyaan 1 per 1
                                                    for (document in documents) {
                                                        val questionId =
                                                            document.data["questionId"].toString()
                                                        listOfQuiz.add(questionId)
                                                    }
                                                    Handler().postDelayed({
                                                        for (index in listOfQuiz.indices) {
                                                            FirebaseFirestore
                                                                .getInstance()
                                                                .collection("quiz")
                                                                .document(listOfQuiz[index])
                                                                .delete()
                                                        }

                                                        /// jika sukses maka item quiz set akan hilang
                                                        mProgressDialog.dismiss()
                                                        quizList.removeAt(adapterPosition)
                                                        notifyDataSetChanged()
                                                    }, 1000)
                                                }
                                        } else {
                                            mProgressDialog.dismiss()
                                            Toast.makeText(
                                                itemView.context,
                                                "Failure delete",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }


                        }
                        .setNegativeButton("NO", null)
                        .show()
                }

            }
        }

    }

    /// untuk memanggil file item_quiz yang berisi xml desain untuk menampilkan data quiz
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuizBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    /// fungsi untuk mendapatkan data yang ditampung dalam arraylist (quizList)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(quizList[position])
    }

    /// fungsi untuk mendapatkan seberapa banyak quiz set yang sudah dibuat sebelumnya
    override fun getItemCount(): Int = quizList.size
}