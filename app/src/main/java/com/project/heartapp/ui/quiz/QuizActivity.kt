package com.project.heartapp.ui.quiz

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.project.heartapp.databinding.ActivityQuizBinding
import android.content.DialogInterface

import androidx.appcompat.app.AlertDialog

import androidx.core.content.ContextCompat
import com.project.heartapp.R
import com.bumptech.glide.Glide

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import android.view.KeyEvent
import android.widget.TextView

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.project.heartapp.ui.HomepageActivity
import com.project.heartapp.ui.dashboard.QuizDashboardModel
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class QuizActivity : AppCompatActivity() {

    /// activity ini merupakan activity pengerjaan quiz

    /// inisiasi variabel global membantu aplikasi agar lebih efisien
    private var binding: ActivityQuizBinding? = null
    private val uid: String = FirebaseAuth.getInstance().currentUser!!.uid
    private val SHARED_PREFS = "sharedPrefs"
    private var questionSection = 0
    private val questionList: ArrayList<QuizModel> = ArrayList()
    private var ans = ""
    private val questionAnswer: ArrayList<String> = ArrayList()
    private val questionImage: ArrayList<String> = ArrayList()
    private val question: ArrayList<String> = ArrayList()
    private var time: Long? = 0L
    private var delay: Long = 0L
    private var model: QuizDashboardModel? = null
    private var role: String? = null

    /// mula mula cek role pengguna sekarang
    override fun onResume() {
        super.onResume()
        checkRole()
    }


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        delay = intent.getLongExtra(TIME, 0L)
        model = intent.getParcelableExtra<QuizDashboardModel>(EXTRA_QUIZ) as QuizDashboardModel


        /// ketika klik tombol info, maka akan tampil dialog mengenai info quiz
        binding?.info?.setOnClickListener {
            showInfo()
        }

        /// ketika role adalah admin, maka kita bisa menambahkan soal baru
        binding?.add?.setOnClickListener {
            val intent = Intent(this, QuizAddActivity::class.java)
            intent.putExtra(QuizAddActivity.EXTRA_ADD, model?.quizId)
            startActivity(intent)
        }

        // pilihan jawaban pilihan ganda
        /// ada 4 pilihan, jadi tombol yang kita pilih akan berganti warna menjadi warna merah
        /// misal klik jawaban B, maka tombol B akan berubah warna menjadi merah, yang lainnya putih
        answerChoice()


        /// ketika user klik prev button maka akan meload pertanyaan sebelum nya
        // pertanyaan sebelumnya
        binding!!.prevBtn.setOnClickListener {
            /// akan menampilkan question section sebelumnya
            /// question section itu nomor quiz
            /// misal ada 10 soal quiz
            /// maka ada 10 questionSection
            /// ketika klik prev, maka akan ke questionSection sebelumnhya
            questionSection--
            if (questionSection == 0) {
                if (questionList.size == 2) {
                    binding!!.finishBtn.visibility = View.INVISIBLE
                }
                binding!!.prevBtn.visibility = View.GONE
            } else if (questionSection == questionList.size - 2) {
                binding!!.nextBtn.visibility = View.VISIBLE
                binding!!.finishBtn.visibility = View.INVISIBLE
            }
            val sharedPreferences = getSharedPreferences(
                SHARED_PREFS,
                MODE_PRIVATE
            )
            ans = sharedPreferences.getString(questionSection.toString(), "")!!
            /// netralkan warna pilihan A, B, C, D yang tidak dipilih user
            neutralizeAnswerChoiceBackgroundColor()
            setQuiz()

            /// ketika user sebelumnya pilih A, maka tombol A akan berwarna merah, begitu juga B,C,D
            when (ans) {
                "A" -> {
                    binding?.a?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded)
                    binding?.a?.setTextColor(ContextCompat.getColor(this, R.color.white))

                }
                "B" -> {
                    binding?.b?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded)
                    binding?.b?.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
                "C" -> {
                    binding?.c?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded)
                    binding?.c?.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
                "D" -> {
                    binding?.d?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded)
                    binding?.d?.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
            }
            binding?.textView5?.text = "${questionSection+1}/${questionList.size} "
        }


        /// ketika user klik next button maka akan meload pertanyaan selanjutnya nya
        // pertanyaan berikutnya
        binding!!.nextBtn.setOnClickListener {
            /// akan menampilkan question section selanjutnya
            /// question section itu nomor quiz
            /// misal ada 10 soal quiz
            /// maka ada 10 questionSection
            /// ketika klik next, maka akan ke questionSection selanjutnya

            // simpan jawaban ke dalam memori shared preferences
            val sharedPreferences = getSharedPreferences(
                SHARED_PREFS,
                MODE_PRIVATE
            )
            val editor = sharedPreferences.edit()
            editor.putString(questionSection.toString(), ans)
            editor.apply()

            // load pertanyaan selanjutnya
            questionSection++
            if (questionSection == questionList.size - 1) {
                binding!!.nextBtn.visibility = View.GONE
                binding!!.finishBtn.visibility = View.VISIBLE
                binding!!.prevBtn.visibility = View.VISIBLE
            } else if (questionSection == 1) {
                binding!!.prevBtn.visibility = View.VISIBLE
            }
            ans = sharedPreferences.getString(questionSection.toString(), "")!!
            /// netralkan warna pilihan A, B, C, D yang tidak dipilih user
            neutralizeAnswerChoiceBackgroundColor()
            setQuiz()
            /// ketika user sebelumnya pilih A, maka tombol A akan berwarna merah, begitu juga B,C,D
            when (ans) {
                "A" -> {
                    binding?.a?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded)
                    binding?.a?.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
                "B" -> {
                    binding?.b?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded)
                    binding?.b?.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
                "C" -> {
                    binding?.c?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded)
                    binding?.c?.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
                "D" -> {
                    binding?.d?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded)
                    binding?.d?.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
            }
            binding?.textView5?.text = "${questionSection+1}/${questionList.size} "
        }


        /// jika user adalah admin, maka akan ada tombol untuk edit pertanyaan quiz pada activity edit
        // edit pertanyaan quiz
        binding?.edit?.setOnClickListener {
            val intent = Intent(this, QuizEditActivity::class.java)
            intent.putExtra(QuizEditActivity.QUESTION, questionList[questionSection].question)
            intent.putExtra(
                QuizEditActivity.QUESTION_IMAGE,
                questionList[questionSection].image
            )
            intent.putExtra(QuizEditActivity.A, questionList[questionSection].a)
            intent.putExtra(QuizEditActivity.B, questionList[questionSection].b)
            intent.putExtra(QuizEditActivity.C, questionList[questionSection].c)
            intent.putExtra(QuizEditActivity.D, questionList[questionSection].d)
            intent.putExtra(QuizEditActivity.ANSWER, questionList[questionSection].answer)
            intent.putExtra(
                QuizEditActivity.QUESTION_ID,
                questionList[questionSection].questionId
            )
            startActivity(intent)
        }


        /// jika user adalah admin, maka akan ada tombol delete pertanyaan quiz pada nomor quiz yang saat ini dipilih
        // delete pertanyaan quiz
        binding?.delete?.setOnClickListener { showConfirmDeleteQuestionDialog() }


        /// ketika sudah di ujung nomor quiz akan muncul finish untuk menyelesaikan quiz
        // klik finish pada quiz
        binding!!.finishBtn.setOnClickListener {
            showPopupConfirmationFinishQuiz()
        }

        /// munculkan timer atau waktu hitung mundur dari waktu quiz
        timer = object : CountDownTimer(delay, 1000) {
            override fun onFinish() {
                showQuizResult()
            }

            override fun onTick(delay: Long) {
                time = delay
                binding?.countdownTimer?.text = getString(
                    R.string.formatted_time,
                    TimeUnit.MILLISECONDS.toMinutes(delay) % 60,
                    TimeUnit.MILLISECONDS.toSeconds(delay) % 60
                )
            }
        }.start()
    }

    /// Fungsi untuk menampilkan alert dialog konformasi menyelesaikan quiz
    private fun showPopupConfirmationFinishQuiz() {
        val btnSubmit: Button
        val btnCancel: Button
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_finish_quiz)
        dialog.setCanceledOnTouchOutside(false)
        btnSubmit = dialog.findViewById(R.id.submit)
        btnCancel = dialog.findViewById(R.id.cancel)

        btnSubmit.setOnClickListener {
            dialog.dismiss()
            showQuizResult()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    /// fungsi untuk menampilkan hasil dari quiz yang user kerjakan
    private fun showQuizResult() {
        val btnSubmit: Button
        val resultPoint: TextView
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_quiz_finish)
        dialog.setCanceledOnTouchOutside(false)
        btnSubmit = dialog.findViewById(R.id.submit)
        resultPoint = dialog.findViewById(R.id.score)


        // simpan jawaban soal terakhir
        val sharedPreferences = getSharedPreferences(
            SHARED_PREFS,
            MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putString(questionSection.toString(), ans)
        editor.apply()

        var correctAns = 0
        var failureAns = 0
        for (i in questionList.indices) {
            questionList[i].answer?.let { questionAnswer.add(it) }
            questionList[i].question?.let { question.add(it) }
            questionList[i].image?.let { questionImage.add(it) }
            if (sharedPreferences.getString(i.toString(), "") == questionList[i]
                    .answer
            ) {
                correctAns++
            } else {
                failureAns++
            }
        }


        /// hitung total soal dan total jawaban benar
        val questionSize = questionList.size
        // hitung total skor
        val score = correctAns.toString().toDouble() / questionSize.toString().toDouble() * 100
        val decimalFormat = DecimalFormat("0.0")
        /// munculkan nilai dari 0 - 100
        resultPoint.text = decimalFormat.format(score)


        /// klik GO HOMEPAGE
        btnSubmit.setOnClickListener {
            //hapus semua jawaban tersimpan
            sharedPreferences.edit().clear().apply()
            dialog.dismiss()
            val intent = Intent(this, HomepageActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    /// fungsi untuk mengecek role user saat ini, jika admin maka bisa Create soal, Edit soal, dan delete soal
    private fun checkRole() {
        FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {
                role = it.data?.get("role").toString()
                if(role == "admin") {
                    binding?.add?.visibility = View.VISIBLE
                }
                populateQuiz()
            }
    }

    /// ketika klik tombol info, maka akan tampil dialog mengenai info quiz
    private fun showInfo() {
        val btnSubmit: Button
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_info)
        dialog.setCanceledOnTouchOutside(false)
        btnSubmit = dialog.findViewById(R.id.submit)

        btnSubmit.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }


    /// opsi untuk kembali ke halaman Quiz dashboard
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder(this)
                .setTitle("Confirm Exit")
                .setMessage("Are you sure want to exit quiz ?")
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setPositiveButton(
                    "YES",
                ) { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    //hapus semua jawaban tersimpan
                    val delete =
                        getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
                    delete.edit().clear().apply()
                    dialogInterface.dismiss()
                    onBackPressed()
                }
                .setNegativeButton("NO", null)
                .show()
            return true
        }
        return false
    }


    /// fungsi untuk menampilkan quiz section
    /// misal quiz nomor 1, maka di fungsi inilah semuanya ditampilkan
    /// dari mulai gambar, soal, pilihan A, B, C, D, dan button next prev
    @SuppressLint("SetTextI18n")
    private fun setQuiz() {
        if (questionList.size > 0) {
            binding!!.questionSet.visibility = View.VISIBLE

            binding?.textView5?.text = "1/${questionList.size}"

            val questionImage: String? = questionList[questionSection].image
            if (questionImage != "") {
                binding?.noPic?.visibility = View.GONE
                binding!!.imageQuiz.visibility = View.VISIBLE
                Glide.with(this)
                    .load(questionImage)
                    .into(binding!!.imageQuiz)
            } else {
                binding!!.imageQuiz.visibility = View.INVISIBLE
                binding?.noPic?.visibility = View.VISIBLE
            }
            binding!!.question.text = questionList[questionSection].question
            binding!!.a.text = "A. " + questionList[questionSection].a
            binding!!.b.text = "B. " + questionList[questionSection].b
            binding!!.c.text = "C. " + questionList[questionSection].c
            binding!!.d.text = "D. " + questionList[questionSection].d
            binding!!.noData.visibility = View.GONE
            if (role == "admin") {
                binding?.edit?.visibility = View.VISIBLE
                binding?.delete?.visibility = View.VISIBLE
            }
            if (questionList.size > 1 && questionSection < questionList.size - 1) {
                binding!!.nextBtn.visibility = View.VISIBLE
                binding!!.finishBtn.visibility = View.INVISIBLE
            } else if (questionList.size == 1) {
                binding!!.nextBtn.visibility = View.GONE
                binding!!.finishBtn.visibility = View.VISIBLE
            }
        } else {
            binding!!.noData.visibility = View.VISIBLE
            binding!!.nextBtn.visibility = View.GONE
            binding!!.questionSet.visibility = View.GONE
            binding!!.finishBtn.visibility = View.INVISIBLE
            binding!!.prevBtn.visibility = View.GONE
            binding?.edit?.visibility = View.GONE
            binding?.delete?.visibility = View.GONE
        }
    }

    /// ambil soal soal dari database dari quiz set yang dipilih
    /// setelah soal diambil, masukkan ke kelas QuizModel,
    /// dari kelas quiz model akan di jadikan questionSection
    private fun populateQuiz() {
        binding!!.progressBar.visibility = View.VISIBLE
        questionList.clear()
        FirebaseFirestore
            .getInstance()
            .collection("quiz")
            .whereEqualTo("quizId", model?.quizId)
            .get()
            .addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val model = QuizModel()
                        model.a = "" + document["a"]
                        model.answer = "" + document["answer"]
                        model.b = "" + document["b"]
                        model.c = "" + document["c"]
                        model.d = "" + document["d"]
                        model.question = "" + document["question"]
                        model.image = "" + document["image"]
                        model.questionId = "" + document["questionId"]
                        questionList.add(model)
                    }

                    // set quiz jika ada
                    setQuiz()
                    binding!!.progressBar.visibility = View.GONE
                } else {
                    binding!!.progressBar.visibility = View.GONE
                    binding!!.noData.visibility = View.VISIBLE
                }
            }
    }


    /// fugnsi untuk mengubah warna dari pilihan yang kita pilih
    /// contoh: pilih A, maka tombol a akan berubah menjadi warna merah
    private fun answerChoice() {
        binding!!.a.setOnClickListener {
            ans = "A"
            binding?.d?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
            binding?.d?.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding?.b?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
            binding?.b?.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding?.c?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
            binding?.c?.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding?.a?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded)
            binding?.a?.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        binding!!.b.setOnClickListener {
            ans = "B"
            binding?.a?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
            binding?.a?.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding?.d?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
            binding?.d?.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding?.c?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
            binding?.c?.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding?.b?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded)
            binding?.b?.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        binding!!.c.setOnClickListener {
            ans = "C"
            binding?.a?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
            binding?.a?.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding?.b?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
            binding?.b?.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding?.d?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
            binding?.d?.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding?.c?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded)
            binding?.c?.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        binding!!.d.setOnClickListener {
            ans = "D"
            binding?.a?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
            binding?.a?.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding?.b?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
            binding?.b?.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding?.c?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
            binding?.c?.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding?.d?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded)
            binding?.d?.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
    }


    /// fungsi untuk menetralkan warna
    /// misal dari yang tadinya warna merah berubah menjadi warna putih
    private fun neutralizeAnswerChoiceBackgroundColor() {
        binding?.a?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
        binding?.a?.setTextColor(ContextCompat.getColor(this, R.color.black))

        binding?.b?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
        binding?.b?.setTextColor(ContextCompat.getColor(this, R.color.black))

        binding?.c?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
        binding?.c?.setTextColor(ContextCompat.getColor(this, R.color.black))

        binding?.d?.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
        binding?.d?.setTextColor(ContextCompat.getColor(this, R.color.black))
    }


    /// fungsi untuk memunculkan pemberitahuan sebelum menghapus quiz
    private fun showConfirmDeleteQuestionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete Question")
            .setMessage("Are you sure want to delete this question ?")
            .setIcon(R.drawable.ic_baseline_warning_24)
            .setPositiveButton(
                "YES",
            ) { dialogInterface: DialogInterface, _: Int ->
                FirebaseFirestore
                    .getInstance()
                    .collection("quiz")
                    .document(questionList[questionSection].questionId!!)
                    .delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showSuccessDialog()
                        } else {
                            showFailureDialog()
                        }
                        dialogInterface.dismiss()
                    }
            }
            .setNegativeButton("NO", null)
            .show()
    }


    private fun showFailureDialog() {
        AlertDialog.Builder(this)
            .setTitle("Failure Delete Question")
            .setMessage("Please check your internet connection, and try again later")
            .setIcon(R.drawable.ic_baseline_clear_24)
            .setPositiveButton(
                "OK",
            ) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
            .show()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success Delete Question")
            .setMessage("Success delete this question")
            .setIcon(R.drawable.ic_baseline_check_circle_outline_24)
            .setPositiveButton(
                "OK",
            ) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                questionSection = 0
                binding!!.prevBtn.visibility = View.GONE
                populateQuiz()
            }
            .show()
    }


    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        binding = null
    }

    /// untuk membuat inisiasi variabel yang akan digunakan di method onCreate diatas
    companion object {
        const val EXTRA_QUIZ = "quiz"
        const val TIME = "time"
        lateinit var timer: CountDownTimer
    }
}