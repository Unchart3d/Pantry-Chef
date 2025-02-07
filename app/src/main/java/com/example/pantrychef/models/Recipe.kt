package com.example.pantrychef.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    val id: Int,
    val image: String?,
    val title: String,

    val missedIngredients: List<MissedIngredient>?
) : Parcelable

@Parcelize
data class MissedIngredient(
    val name: String,
    val original: String
) : Parcelable
