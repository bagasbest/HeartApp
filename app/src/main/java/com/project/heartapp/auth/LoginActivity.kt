package com.project.heartapp.auth

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.heartapp.databinding.ActivityLoginBinding
import com.project.heartapp.ui.HomepageActivity

class LoginActivity : AppCompatActivity() {

    private var binding: ActivityLoginBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        /// fungsi untuk melakukan auto login pada user yang sebelumnya pernah login
        autoLogin()

        /// ketika user klik Sign Up maka ke halaman registrasi
        binding?.signUp?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        /// ketika klik login maka akan ada validasi terlebih dahulu
        binding?.login?.setOnClickListener {
            formValidation()
        }
    }

    /// fungsi untuk melakukan auto login pada user yang sebelumnya pernah login
    private fun autoLogin() {
        if(FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, HomepageActivity::class.java))
            finish()
        }
    }



    /// Fungsi untuk memvalidasi username dan password dengan cara mencari di database, apakah ada data yang terdaftar
    private fun formValidation() {
        val username = binding?.username?.text.toString().trim()
        val password = binding?.password?.text.toString().trim()

        if(username.isEmpty()) {
            Toast.makeText(this, "Username tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val mProgressDialog = ProgressDialog(this)
        mProgressDialog.setMessage("Mohon tunggu hingga proses selesai...")
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()

        FirebaseFirestore
            .getInstance()
            .collection("users")
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .addOnCompleteListener(OnCompleteListener { task ->
                if (task.result.size() == 0) {
                    /// jika tidak terdapat di database dan email serta password, maka tidak bisa login
                    mProgressDialog.dismiss()
                    showFailureDialog()
                    return@OnCompleteListener
                }

                /// jika terdaftar maka ambil email di database, kemudian lakukan autentikasi menggunakan email & password dari user
                for (snapshot in task.result) {
                    val email = "" + snapshot["email"]

                    /// fungsi untuk mengecek, apakah email yang di inputkan ketika login sudah terdaftar di database atau belum
                    FirebaseAuth
                        .getInstance()
                        .signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                /// jika terdapat di database dan email serta password sama, maka masuk ke homepage
                                mProgressDialog.dismiss()
                                startActivity(Intent(this, HomepageActivity::class.java))
                                finish()
                            } else {
                                /// jika tidak terdapat di database dan email serta password, maka tidak bisa login
                                mProgressDialog.dismiss()
                                showFailureDialog()
                            }
                        }
                }
            })
    }

    /// munculkan dialog ketika gagal login
    private fun showFailureDialog() {
        AlertDialog.Builder(this)
            .setTitle("Gagal melakukan login")
            .setMessage("Silahkan login kembali dengan informasi yang benar")
            .setPositiveButton("OKE") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}