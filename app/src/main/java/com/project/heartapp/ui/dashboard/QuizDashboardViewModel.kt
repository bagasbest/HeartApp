package com.project.heartapp.ui.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

/// kelas view model berfungsi untuk mendapatkan data quiz set dari database
class QuizDashboardViewModel : ViewModel() {

    private val quizList = MutableLiveData<ArrayList<QuizDashboardModel>>()
    private val listItems = ArrayList<QuizDashboardModel>()
    private val TAG = QuizDashboardViewModel::class.java.simpleName

    fun setListQuiz() {
        listItems.clear()


        try {
            /// kelas view model berfungsi untuk mendapatkan data quiz set dari database
            /// pertama cari collection quiz_dashboard di firestore
            /// kemudian ambil semua data data yang tersimpan
            /// yaitu quiz set
            /// setelah data di ambil, masukkan kedalam kelas model nya
            /// setelah itu transfer ke QuizDashboardActivity untuk ditampilkan dalam bentuk list malalui adapter
            FirebaseFirestore.getInstance().collection("quiz_dashboard")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val model = QuizDashboardModel()
                        model.quizId = document.data["quizId"].toString()
                        model.title = document.data["title"].toString()
                        model.time = document.data["time"] as Long

                        listItems.add(model)
                    }
                    quizList.postValue(listItems)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    fun getQuizList(): LiveData<ArrayList<QuizDashboardModel>> {
        return quizList
    }

}