package com.example.pantrychef.api

import com.example.pantrychef.models.Recipe
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SpoonacularApi {
    @GET("recipes/findByIngredients")
    fun getRecipesByIngredients(
        @Query("apiKey") apiKey: String,
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 100
    ): Call<List<Recipe>>
}