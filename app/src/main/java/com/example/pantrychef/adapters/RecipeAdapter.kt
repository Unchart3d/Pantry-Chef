package com.example.pantrychef.adapters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.pantrychef.models.Recipe
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale

@Composable
fun RecipeList(recipes: List<Recipe>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(recipes) { recipe ->
            RecipeItem(recipe)
        }
    }
}

@Composable
fun RecipeItem(recipe: Recipe) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Handle item click
            },
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(recipe.image)
                        .crossfade(true)
                        .scale(Scale.FILL) // Important: Fill the bounds
                        .build(),
                    contentScale = ContentScale.Crop,
                )


                Image(
                    painter = painter,
                    contentDescription = recipe.title,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(end = 16.dp),
                )

                Text(
                    text = recipe.title ?: "", // Handle null title
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
            }

            // Missing ingredients section (with null checks)
            val missingIngredientsText = recipe.missedIngredients?.takeIf { it.isNotEmpty() }?.joinToString(", ") { it?.name ?: "Unknown Ingredient" } ?: "No missing ingredients"

            Text(
                text = missingIngredientsText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp) // Consistent padding
            )
        }
    }
}