package com.project.heartapp.ui.quiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.project.heartapp.databinding.ActivityQuizAddBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.bumptech.glide.Glide
import android.app.ProgressDialog
import com.google.firebase.storage.FirebaseStorage
import android.content.Intent
import android.app.AlertDialog
import android.net.Uri
import com.project.heartapp.R
import java.lang.Exception


class QuizAddActivity : AppCompatActivity() {

    /// activitty untuk buat soal baru
    private var binding: ActivityQuizAddBinding? = null
    private var dp: String? = null
    private val REQUEST_FROM_GALLERY = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizAddBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        /// klik kembali ke halaman sebelumnya
        binding?.back?.setOnClickListener {
            onBackPressed()
        }

        /// klik save soal quiz
        /// akan ada validasi sebelum menyimpan soal quiz
        binding?.save?.setOnClickListener {
            formValidation()
        }

        // tambahkan gambar jika ada
        binding!!.imageHint.setOnClickListener {
            ImagePicker.with(this@QuizAddActivity)
                .galleryOnly()
                .compress(1024)
                .start(REQUEST_FROM_GALLERY)
        }
    }

    /// fungsi untuk memvalidasi sebelum menyimpan soal quiz ke dalam database
    /// validasi meliputi user harus mengisi semua kolom yang ada
    private fun formValidation() {
        val question: String = binding?.question?.text.toString()
        val a: String = binding?.a?.text.toString().trim()
        val b: String = binding?.b?.text.toString().trim()
        val c: String = binding?.c?.text.toString().trim()
        val d: String = binding?.d?.text.toString().trim()
        val answer: String = binding?.answer?.text.toString().trim()

        when {
            question.isEmpty() -> {
                Toast.makeText(this@QuizAddActivity, "Question must be filled", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            a.isEmpty() -> {
                Toast.makeText(this@QuizAddActivity, "Answer option 1 must be filled", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            b.isEmpty() -> {
                Toast.makeText(this@QuizAddActivity, "Answer option 2 must be filled", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            c.isEmpty() -> {
                Toast.makeText(this@QuizAddActivity, "Answer option 3 must be filled", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            d.isEmpty() -> {
                Toast.makeText(this@QuizAddActivity, "Answer option 4 must be filled", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            answer.isEmpty() -> {
                Toast.makeText(this@QuizAddActivity, "Answer must be filled", Toast.LENGTH_SHORT)
                    .show()
                return
            }

            // simpan soal ke firebase
            else -> {
                binding?.progressBar?.visibility = View.VISIBLE

                val questionId = System.currentTimeMillis().toString()
                val quizId = intent.getStringExtra(EXTRA_ADD)

                val questionMap: MutableMap<String, Any> = HashMap()
                questionMap["questionId"] = questionId
                questionMap["question"] = question
                if (dp != null) {
                    questionMap["image"] = dp!!
                } else {
                    questionMap["image"] = ""
                }
                questionMap["a"] = a
                questionMap["b"] = b
                questionMap["c"] = c
                questionMap["d"] = d
                questionMap["answer"] = answer
                questionMap["quizId"] = quizId.toString()


                FirebaseFirestore
                    .getInstance()
                    .collection("quiz")
                    .document(questionId)
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
        }
    }




    /// FUngsi untuk mengupload gambar soal dan menyimpan dalam cloud storage
    private fun uploadArticleDp(data: Uri?) {
        val mStorageRef = FirebaseStorage.getInstance().reference
        val mProgressDialog = ProgressDialog(this)
        mProgressDialog.setMessage("Please wait until process finish...")
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
                            binding!!.imageHint.visibility = View.GONE
                            Glide
                                .with(this)
                                .load(dp)
                                .into(binding!!.image)
                        }
                        .addOnFailureListener { e: Exception? ->
                            mProgressDialog.dismiss()
                            Toast.makeText(
                                this@QuizAddActivity,
                                "Failure upload image",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                }
                .addOnFailureListener { e: Exception? ->
                    mProgressDialog.dismiss()
                    Toast.makeText(this@QuizAddActivity, "Failure upload image", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }


    private fun showFailureDialog() {
        AlertDialog.Builder(this)
            .setTitle("Failure Create Question")
            .setMessage("Please check your internet connection, and try again later")
            .setIcon(R.drawable.ic_baseline_clear_24)
            .setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success Create Question")
            .setMessage("Question will be add soon")
            .setIcon(R.drawable.ic_baseline_check_circle_outline_24)
            .setPositiveButton("OK") { dialogInterface, _ ->
                dialogInterface.dismiss()
                onBackPressed()
            }
            .show()
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

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object {
        const val EXTRA_ADD = "add"
    }
}