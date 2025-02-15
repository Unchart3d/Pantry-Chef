package com.example.pantrychef

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.pantrychef.ui.theme.PantryChefTheme

class FavoritesScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FavoritesScreenContent()
        }
    }
}

@Composable
fun FavoritesScreenContent() {
    val context = LocalContext.current
    val favorites = remember {
        mutableStateOf(getSavedFavorites(context))
    }

    val backgroundImage = painterResource(R.drawable.ingredient_background)

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                text = "Favorites",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(8.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(favorites.value) { recipe ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updatedFavorites = favorites.value.toMutableList().apply {
                                    remove(recipe)
                                }
                                favorites.value = updatedFavorites
                                saveFavorites(updatedFavorites, context)
                            },
                        elevation = CardDefaults.elevatedCardElevation(8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = recipe.title ?: "No title", modifier = Modifier.padding(8.dp))
                            // Add more details for each favorite recipe here
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
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Button(
                    onClick = {
                        (context as? Activity)?.finish()
                    },
                    modifier = Modifier.fillMaxWidth().padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557))
                ) {
                    Text("Back")
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    PantryChefTheme {
        FavoritesScreenContent()
    }
}

