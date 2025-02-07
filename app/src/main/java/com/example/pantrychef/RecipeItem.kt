package com.example.pantrychef

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.example.pantrychef.models.Recipe

@Composable
fun RecipeItem(recipe: Recipe) {
    var showMissingIngredients by remember { mutableStateOf(false) }

    var isFavorite by remember { mutableStateOf(false) } // Track whether the recipe is favorited

    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showMissingIngredients = !showMissingIngredients // Toggle missing ingredients
            },
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                contentScale = ContentScale.Crop,
            )

            Image(
                painter = painter,
                contentDescription = recipe.title,
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 8.dp)
            )

            // Favorite star button
            IconButton(
                onClick = {
                    isFavorite = !isFavorite
                    // Handle adding/removing from favorites logic
                }
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "Favorite Star",
                    modifier = Modifier.size(24.dp)
                )
            }

            if (showMissingIngredients) {
                Text(
                    text = "Missing Ingredients:",
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                val missingIngredientsText = recipe.missedIngredients?.takeIf { it.isNotEmpty() }
                    ?.joinToString(", ") { it.name ?: "Unknown Ingredient" } ?: "No missing ingredients"

                Text(
                    text = missingIngredientsText,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }
    }
}
