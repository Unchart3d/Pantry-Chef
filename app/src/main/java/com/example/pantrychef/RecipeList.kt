package com.example.pantrychef

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.example.pantrychef.models.MissedIngredient
import com.example.pantrychef.models.Recipe
import com.example.pantrychef.ui.theme.PantryChefTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class RecipeListScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val recipesJson = intent.getStringExtra("recipes_list")
        Log.d("CACHE_DEBUG", "Received JSON: $recipesJson")

        if (recipesJson.isNullOrEmpty()) {
            Toast.makeText(this, "No recipes found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val recipes: List<Recipe> = Gson().fromJson(
                recipesJson,
                object : TypeToken<List<Recipe>>() {}.type
            )
            Log.d("CACHE_DEBUG", "Parsed recipes: $recipes")

            setContent {
                RecipeListScreenContent(recipes = recipes)
            }

        } catch (e: Exception) {
            Log.e("CACHE_ERROR", "Error parsing recipes: ${e.message}")
            Toast.makeText(this, "Error loading recipes", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    fun RecipeListScreenContent(recipes: List<Recipe>) {
        val context = LocalContext.current
        var favorites by remember { mutableStateOf(getSavedFavorites(context)) }

        val handleFavoriteClick = { recipe: Recipe ->
            favorites = if (favorites.any { it.id == recipe.id }) {
                favorites.filter { it.id != recipe.id }
            } else {
                favorites + recipe
            }
            saveFavorites(favorites, context)
        }

        val backgroundImage = painterResource(R.drawable.ingredient_background)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Image(
                painter = backgroundImage,
                contentDescription = "",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Pantry Chef",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(8.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f).fillMaxSize()) {
                    items(recipes) { recipe ->
                        RecipeItem(recipe, handleFavoriteClick, favorites.any { it.id == recipe.id })
                    }
                }

                Button(
                    onClick = { context.startActivity(Intent(context, MainActivity::class.java)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557))
                ) {
                    Text("Ingredients")
                }

                Button(
                    onClick = { context.startActivity(Intent(context, FavoritesScreen::class.java)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557))
                ) {
                    Text("Favorites")
                }
            }
        }
    }

    @Composable
    fun RecipeItem(recipe: Recipe, onFavoriteClick: (Recipe) -> Unit, isFavorite: Boolean) {
        var showDetails by remember { mutableStateOf(false) }
        var instructions by remember { mutableStateOf<String?>(null) }
        val context = LocalContext.current

        fun fetchRecipeInstructions(recipeId: Int) {
            val apiKey = apikey.API_KEY
            val url = "https://api.spoonacular.com/recipes/$recipeId/information?apiKey=$apiKey"

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = URL(url).readText()
                    val jsonObject = JSONObject(response)
                    val analyzedInstructions = jsonObject.optJSONArray("analyzedInstructions")

                    val steps = analyzedInstructions?.let {
                        if (it.length() > 0) {
                            val stepsArray = it.getJSONObject(0).optJSONArray("steps")
                            stepsArray?.let { stepsJson ->
                                (0 until stepsJson.length()).joinToString("\n") { index ->
                                    "${index + 1}. ${stepsJson.getJSONObject(index).getString("step")}"
                                }
                            }
                        } else null
                    } ?: "No instructions available."

                    withContext(Dispatchers.Main) {
                        instructions = steps
                    }
                } catch (e: Exception) {
                    Log.e("API_ERROR", "Failed to fetch instructions: ${e.message}")
                    withContext(Dispatchers.Main) {
                        instructions = "Error fetching instructions."
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().clickable {
                showDetails = !showDetails
                if (showDetails && instructions == null) {
                    fetchRecipeInstructions(recipe.id)
                }
            },
            elevation = CardDefaults.elevatedCardElevation(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = recipe.title ?: "Unknown Title", style = MaterialTheme.typography.headlineSmall)

                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context).data(recipe.image).crossfade(true).scale(Scale.FILL).build(),
                    contentScale = ContentScale.Crop
                )

                Image(painter = painter, contentDescription = recipe.title, modifier = Modifier.size(100.dp))

                if (showDetails) {
                    recipe.missedIngredients?.takeIf { it.isNotEmpty() }?.let {
                        Text("Missing Ingredients:", color = MaterialTheme.colorScheme.error)
                        it.forEach { ingredient -> Text("- ${ingredient.original}") }
                    } ?: Text("No missing ingredients!", color = Color.Green)

                    instructions?.let {
                        Text("\nInstructions:\n$it")
                    }
                }

                IconButton(onClick = { onFavoriteClick(recipe) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFE91E63) else Color.Gray
                    )
                }
            }
        }
    }
}

fun saveFavorites(favorites: List<Recipe>, context: Context) {
    val sharedPreferences = context.getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("FavoritesList", Gson().toJson(favorites)).apply()
}

fun getSavedFavorites(context: Context): List<Recipe> {
    val json = context.getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE).getString("FavoritesList", null)
    return json?.let { Gson().fromJson(it, object : TypeToken<List<Recipe>>() {}.type) } ?: emptyList()
}


@Preview(showBackground = true)
@Composable
fun RecipeListScreenPreview() {

    val sampleRecipes = listOf(
        Recipe(
            id = 1234,
            title = "Spaghetti Bolognese",
            image = "https://example.com/spaghetti.jpg",
            missedIngredients = listOf(
                MissedIngredient(name = "Garlic", original = "Fresh garlic"),
                MissedIngredient(name = "Tomato", original = "Roma tomato")
            )
        ),
        Recipe(
            id = 2555,
            title = "Chicken Curry",
            image = "https://example.com/chicken_curry.jpg",
            missedIngredients = listOf(
                MissedIngredient(name = "Coriander", original = "Fresh coriander leaves"),
                MissedIngredient(name = "Cumin", original = "Ground cumin")
            )
        ),
        Recipe(
            id = 65454,
            title = "Caesar Salad",
            image = "https://example.com/caesar_salad.jpg",
            missedIngredients = emptyList()
        )
    )

    PantryChefTheme {
        RecipeListScreen().RecipeListScreenContent(recipes = sampleRecipes)
    }
}
