package com.project.heartapp.auth

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.heartapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private var binding: ActivityRegisterBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        /// ketika user klik registrasi, maka akan ada validasi terlebih dahulu pada kolom yang user inputkan
        binding?.signUp?.setOnClickListener {
            formValidation()
        }

    }

    /// ketika user klik registrasi, maka akan ada validasi terlebih dahulu pada kolom yang user inputkan
    private fun formValidation() {
        /// yang akan divalidasi yaitu username, password, confirmasi password, dan email
        val username = binding?.username?.text.toString().trim()
        val password = binding?.password?.text.toString().trim()
        val confPass = binding?.confPassword?.text.toString().trim()
        val email = binding?.email?.text.toString().trim()

        when {
            email.isEmpty() -> {
                Toast.makeText(this, "Email harus diisi", Toast.LENGTH_SHORT).show()
                return
            }
            username.isEmpty() -> {
                Toast.makeText(this, "Username harus diisi", Toast.LENGTH_SHORT).show()
                return
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Password harus diisi", Toast.LENGTH_SHORT).show()
                return
            }
            password != confPass -> {
                Toast.makeText(this, "Konfirmasi password tidak sama", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val mProgressDialog = ProgressDialog(this)
        mProgressDialog.setMessage("Mohon tunggu hingga proses selesai...")
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()


        /// proses create akun user menggunakan email dan password
        FirebaseAuth
            .getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful) {


                    /// jika create user sukses, selanjutnya sistem akan menyimpan data data user kedalam database firebase firestore
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val data = mapOf(
                        "uid" to uid,
                        "username" to username,
                        "email" to email,
                        "role" to "user"
                    )
                    FirebaseFirestore
                        .getInstance()
                        .collection("users")
                        .document(uid)
                        .set(data)
                        .addOnCompleteListener { task->
                            if(task.isSuccessful) {
                                mProgressDialog.dismiss()
                                Toast.makeText(this, "Berhasil Registrasi", Toast.LENGTH_SHORT).show()
                                onBackPressed()
                            } else {
                                mProgressDialog.dismiss()
                                Toast.makeText(this, "Gagal registrasi", Toast.LENGTH_SHORT).show()

                            }
                        }
                } else {
                    mProgressDialog.dismiss()
                    Toast.makeText(this, "Gagal registrasi", Toast.LENGTH_SHORT).show()
                }
            }

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}