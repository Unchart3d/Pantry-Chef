package com.example.pantrychef

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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

class RecipeListScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recipesJson = intent.getStringExtra("recipes_list")
        val recipesList: List<Recipe> =
            Gson().fromJson(recipesJson, object : TypeToken<List<Recipe>>() {}.type)


        setContent {
            RecipeListScreenContent(recipes = recipesList)
        }
    }

    @Composable
    fun RecipeListScreenContent(recipes: List<Recipe>) {
        val context = LocalContext.current
        var recipes by remember { mutableStateOf(recipes) }
        var favorites by remember { mutableStateOf(getSavedFavorites(context)) } // Store favorite recipes

        val handleFavoriteClick = { recipe: Recipe ->
            favorites = if (favorites.contains(recipe)) {
                favorites.filter { it != recipe } // Remove from favorites
            } else {
                favorites + recipe // Add to favorites
            }
            saveFavorites(favorites, context)
        }

        val backgroundImage = painterResource(R.drawable.ingredient_background)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ){
            Image(
                painter = backgroundImage,
                contentDescription = "",
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Pantry Chef",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(8.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f).fillMaxSize()) {
                    items(recipes) { recipe ->
                        RecipeItem(recipe = recipe, onFavoriteClick = handleFavoriteClick, isFavorite = favorites.contains(recipe))
                    }
                }

                Button(
                    onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557))
                ) {
                    Text("Ingredients")
                }

                Button(
                    onClick = {
                        val intent = Intent(context, FavoritesScreen::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557))
                ) {
                    Text("Favorites")
                }
            }
        }

    }

    @Composable
    fun RecipeItem(recipe: Recipe, onFavoriteClick: (Recipe) -> Unit, isFavorite: Boolean) {
        var showMissingIngredients by remember { mutableStateOf(false) }
        var favoriteState by remember { mutableStateOf(isFavorite) }


        val context = LocalContext.current

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showMissingIngredients = !showMissingIngredients },
            elevation = CardDefaults.elevatedCardElevation(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = recipe.title ?: "Unknown Title",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(recipe.image)
                        .crossfade(true)
                        .scale(Scale.FILL)
                        .build(),
                    contentScale = ContentScale.Crop
                )

                Image(
                    painter = painter,
                    contentDescription = recipe.title,
                    modifier = Modifier.size(100.dp).padding(bottom = 8.dp)
                )

                if (showMissingIngredients) {
                    if (!recipe.missedIngredients.isNullOrEmpty()) {
                        Text(
                            text = "Missing Ingredients:",
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )

                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            recipe.missedIngredients.forEach { ingredient ->
                                Text(
                                    text = "- ${ingredient.original}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No missing ingredients!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Green),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        favoriteState = !favoriteState
                        onFavoriteClick(recipe) // Handle adding/removing from favorites
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                        tint = if (isFavorite) Color(0xFFE91E63) else Color.Gray
                    )
                }
            }
        }
    }
}

fun saveFavorites(favorites: List<Recipe>, context: Context) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    val gson = Gson()
    val json = gson.toJson(favorites)
    editor.putString("FavoritesList", json)
    editor.apply()
}

fun getSavedFavorites(context: Context): List<Recipe> {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE)
    val gson = Gson()
    val json = sharedPreferences.getString("FavoritesList", null)

    return if (json != null) {
        val type = object : TypeToken<List<Recipe>>() {}.type
        gson.fromJson(json, type)
    } else {
        emptyList()
    }
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
