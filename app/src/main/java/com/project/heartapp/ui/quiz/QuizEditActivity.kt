package com.project.heartapp.ui.quiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.project.heartapp.R
import com.project.heartapp.databinding.ActivityQuizEditBinding
import com.github.dhaval2404.imagepicker.ImagePicker

import com.bumptech.glide.Glide

import android.widget.Toast

import android.app.ProgressDialog

import com.google.firebase.storage.FirebaseStorage

import android.content.Intent

import android.app.AlertDialog
import android.net.Uri
import android.view.View
import com.google.android.gms.tasks.Task

import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception


class QuizEditActivity : AppCompatActivity() {

    /// Activity untuk mengedit soal

    private var binding: ActivityQuizEditBinding? = null
    private var dp: String? = null
    private val REQUEST_FROM_GALLERY = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizEditBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        /// mula mula akan ada soal yang dipilih
        /// setelah itu soal bisa diedit di halaman ini
        if (intent.getStringExtra(QUESTION_IMAGE) != "") {
            Glide.with(this)
                .load(intent.getStringExtra(QUESTION_IMAGE))
                .into(binding!!.image)
        }

        binding?.a?.setText(intent.getStringExtra(A))
        binding?.b?.setText(intent.getStringExtra(B))
        binding?.c?.setText(intent.getStringExtra(C))
        binding?.d?.setText(intent.getStringExtra(D))
        binding?.question?.setText(intent.getStringExtra(QUESTION))
        binding?.answer?.setText(intent.getStringExtra(ANSWER))

        // simpan soal quiz
        binding?.save?.setOnClickListener {
            // validasi kolom isian
            formValidation()
        }


        // tambahkan gambar jika ada
        binding!!.imageHint.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .compress(1024)
                .start(REQUEST_FROM_GALLERY)
        }

        /// kembali ke halaman sebelumnya
        binding?.back?.setOnClickListener {
            onBackPressed()
        }
    }


    /// user harus mengisi semua kolom yang ada
    /// jika tidak diisi 1 kolom pun, akan muncul pemberitahuan bahwa user harus mengisi semua dengan benar
    private fun formValidation() {
        val question: String = binding?.question?.text.toString()
        val a: String = binding?.a?.text.toString().trim()
        val b: String = binding?.b?.text.toString().trim()
        val c: String = binding?.c?.text.toString().trim()
        val d: String = binding?.d?.text.toString().trim()
        val answer: String = binding?.answer?.text.toString().trim()
        when {
            question.isEmpty() -> {
                Toast.makeText(
                    this@QuizEditActivity,
                    "Question must be filled",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            a.isEmpty() -> {
                Toast.makeText(
                    this@QuizEditActivity,
                    "Answer option 1 must be filled",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            b.isEmpty() -> {
                Toast.makeText(
                    this@QuizEditActivity,
                    "Answer option 2 must be filled",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            c.isEmpty() -> {
                Toast.makeText(
                    this@QuizEditActivity,
                    "Answer option 3 must be filled",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            d.isEmpty() -> {
                Toast.makeText(
                    this@QuizEditActivity,
                    "Answer option 4 must be filled",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            answer.isEmpty() -> {
                Toast.makeText(this@QuizEditActivity, "Answer must be filled", Toast.LENGTH_SHORT)
                    .show()
                return
            }

            // simpan soal ke firebase
            /// update soal yang telah diedit
            else -> {
                binding!!.progressBar.visibility = View.VISIBLE
                val questionMap: MutableMap<String, Any> = HashMap()
                questionMap["question"] = question
                if (dp != null) {
                    questionMap["image"] = dp!!
                }
                questionMap["a"] = a
                questionMap["b"] = b
                questionMap["c"] = c
                questionMap["d"] = d
                questionMap["answer"] = answer
                FirebaseFirestore
                    .getInstance()
                    .collection("quiz")
                    .document(intent.getStringExtra(QUESTION_ID)!!)
                    .update(questionMap)
                    .addOnCompleteListener { task: Task<Void?> ->
                        if (task.isSuccessful) {
                            binding!!.progressBar.visibility = View.GONE
                            showSuccessDialog()
                        } else {
                            binding!!.progressBar.visibility = View.GONE
                            showFailureDialog()
                        }
                    }
            }
        }

    }

    /// fungsi untuk mengaktifkan permission kamera dan mengambil gambar dari galeri
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_FROM_GALLERY) {
                uploadArticleDp(data?.data)
            }
        }
    }

    /// FUngsi untuk mengupload gambar soal dan menyimpan dalam cloud storage
    private fun uploadArticleDp(data: Uri?) {
        val mStorageRef = FirebaseStorage.getInstance().reference
        val mProgressDialog = ProgressDialog(this)
        mProgressDialog.setMessage("Mohon tunggu hingga proses selesai...")
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()
        val imageFileName = "quiz/data_" + System.currentTimeMillis() + ".png"
        if (data != null) {
            mStorageRef.child(imageFileName).putFile(data)
                .addOnSuccessListener {
                    mStorageRef.child(imageFileName).downloadUrl
                        .addOnSuccessListener { uri: Uri ->
                            mProgressDialog.dismiss()
                            dp = uri.toString()
                            Glide
                                .with(this)
                                .load(dp)
                                .into(binding!!.image)
                        }
                        .addOnFailureListener { e: Exception? ->
                            mProgressDialog.dismiss()
                            Toast.makeText(
                                this@QuizEditActivity,
                                "Gagal mengunggah gambar",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                }
                .addOnFailureListener { e: Exception? ->
                    mProgressDialog.dismiss()
                    Toast.makeText(this@QuizEditActivity, "Gagal mengunggah gambar", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }


    private fun showFailureDialog() {
        AlertDialog.Builder(this)
            .setTitle("Failure Edit Question")
            .setMessage("Please check your internet connection, and try again later")
            .setIcon(R.drawable.ic_baseline_clear_24)
            .setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success Edit Question")
            .setMessage("Question will be updated soon")
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

    companion object {
        const val QUESTION = "question"
        const val A = "a"
        const val B = "b"
        const val C = "c"
        const val D = "d"
        const val ANSWER = "ans"
        const val QUESTION_IMAGE = "img"
        const val QUESTION_ID = "id"
    }
}