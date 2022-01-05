package com.project.heartapp.ui.dashboard

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/// kelas model berfungsi sebagai tempan penampung data quiz set yang sudah didapatkan dari database
/// didalamnya terdapat field field quizId, tittle, dll, yang sesuai pada field di database
@Parcelize
data class QuizDashboardModel (
    var quizId: String? = null,
    var title: String? = null,
    var time: Long = 0L,
) : Parcelable