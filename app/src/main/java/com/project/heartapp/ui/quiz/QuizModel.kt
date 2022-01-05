package com.project.heartapp.ui.quiz

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/// kelas model berfungsi sebagai tempan penampung data soal soal quiz yang sudah didapatkan dari database
/// didalamnya terdapat field field questionId, question, dll, yang sesuai pada field di database
@Parcelize
data class QuizModel(
    var question: String? = null,
    var questionId: String? = null,
    var image: String? = null,
    var a: String? = null,
    var b: String? = null,
    var c: String? = null,
    var d: String? = null,
    var answer: String? = null,
) : Parcelable