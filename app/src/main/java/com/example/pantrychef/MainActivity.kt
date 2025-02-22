package com.example.pantrychef

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pantrychef.models.Recipe
import com.example.pantrychef.ui.theme.PantryChefTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PantryChefScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun PantryChefScreen() {
    val backgroundPainter = painterResource(R.drawable.ingredient_background)
    val context = LocalContext.current
    var ingredientText by remember { mutableStateOf("") }
    var ingredientsList by remember { mutableStateOf(getArrayList(context)) }
    val ingredientsListState = remember { mutableStateOf(ingredientsList) }  // State for the list
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val extras = result.data?.extras
            val bitmap = extras?.get("data") as Bitmap?
            if (bitmap != null) {
                recognizeText(bitmap, ingredientsList, context, ingredientsListState)
            } else {
                Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = backgroundPainter,
                contentDescription = "",
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopEnd,
                modifier = Modifier.fillMaxSize()

            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Pantry Chef", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = ingredientText,
                        onValueChange = { ingredientText = it },
                        label = { Text("Ingredients", color = Color.White) },
                        placeholder = { Text("Type here...", color = Color.LightGray) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            cursorColor = Color(0xFF1D3557),
                            focusedBorderColor = Color(0xFF1D3557),
                            unfocusedBorderColor = Color(0xFF1D3557),
                            focusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                            .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                            .border(2.dp, Color(0xFF1D3557), RoundedCornerShape(8.dp))
                            .shadow(10.dp, shape = RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        launcher.launch(cameraIntent)
                    },colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1D3557))) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Camera", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = {
                        if (ingredientText.isNotEmpty()) {
                            ingredientsList = ArrayList(ingredientsList + ingredientText.trim()) // Update the list
                            saveArrayList(ingredientsList, context)
                            ingredientText = "" // Clear the text field
                            ingredientsListState.value = ingredientsList // Update the list state
                        }
                    },colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557))) {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(ingredientsListState.value) { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val mutableList = ingredientsList.toMutableList()
                                    mutableList.removeAt(index)
                                    ingredientsList = ArrayList(mutableList)
                                    saveArrayList(ingredientsList, context)
                                    ingredientsListState.value = ingredientsList
                                }
                                .padding(8.dp)

                        ) {
                            Text(text = item)
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
                            if (ingredientsList.isEmpty()) {
                                Toast.makeText(context, "Please add ingredients first", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val (lastIngredients, cachedResponse) = getLastQuery(context)

                            if (lastIngredients == ingredientsList && !cachedResponse.isNullOrEmpty()) {
                                // Use cached response
                                Log.d("API_CACHE", "Using cached response")
                                try {
                                    // Use cachedResponse instead of response here
                                    saveLastQuery(context, ingredientsList, cachedResponse) // ← Fix this line
                                    val intent = Intent(context, RecipeListScreen::class.java)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("CACHE_ERROR", "Error using cached response: ${e.message}")
                                    Toast.makeText(context, "Error loading cached recipes", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Make API call since ingredients changed
                                val apiKey = apikey.API_KEY
                                val ingredientsQuery = ingredientsList.joinToString(",") { URLEncoder.encode(it.trim(), StandardCharsets.UTF_8.toString()) }
                                val url = "https://api.spoonacular.com/recipes/findByIngredients?apiKey=$apiKey&ingredients=$ingredientsQuery&number=50"

                                // Modified API call section
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val apiResponse = URL(url).readText()  // Renamed for clarity
                                        val type = object : TypeToken<List<Recipe>>() {}.type
                                        val recipes = Gson().fromJson<List<Recipe>>(apiResponse, type)

                                        withContext(Dispatchers.Main) {
                                            // Save to SharedPreferences and launch activity
                                            saveLastQuery(context, ingredientsList, apiResponse)
                                            val intent = Intent(context, RecipeListScreen::class.java)
                                            context.startActivity(intent)
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "API Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Recipes")
                    }



                    Button(
                        onClick = {
                            val intent = Intent(context, FavoritesScreen::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 36.dp).fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557))
                    ) {
                        Text("Favorites")
                    }
                }

            }
        }
    }
}

fun saveLastQuery(context: Context, ingredients: List<String>, response: String) {
    val sharedPreferences = context.getSharedPreferences("RecipeCache", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("last_response", response)
        apply()  // Using apply() instead of commit() for async operation
    }
}


fun getLastQuery(context: Context): Pair<List<String>, String?> {
    val sharedPreferences = context.getSharedPreferences("RecipeCache", Context.MODE_PRIVATE)

    val jsonIngredients = sharedPreferences.getString("last_ingredients", "[]")
    val lastResponse = sharedPreferences.getString("last_response", null)

    val ingredientsList: List<String> = Gson().fromJson(jsonIngredients, object : TypeToken<List<String>>() {}.type)

    Log.d("CACHE_DEBUG", "Retrieved ingredients: $ingredientsList")
    Log.d("CACHE_DEBUG", "Retrieved response: $lastResponse")

    return Pair(ingredientsList, lastResponse)
}



private fun recognizeText(bitmap: Bitmap, ingredientsList: ArrayList<String>, context: Context, ingredientsListState: MutableState<ArrayList<String>>) {
    val image = InputImage.fromBitmap(bitmap, 0)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    recognizer.process(image)
        .addOnSuccessListener { result ->
            val recognizedIngredients = mutableListOf<String>()
            val textBlocks = result.textBlocks
            for (block in textBlocks) {
                for (line in block.lines) {
                    for (element in line.elements) {
                        val word = element.text
                        recognizedIngredients.add(word)
                    }
                }
            }
            ingredientsList.addAll(recognizedIngredients)
            saveArrayList(ingredientsList, context)
            ingredientsListState.value = ingredientsList // Update the list state
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Failed to recognize text: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}


private fun saveArrayList(list: ArrayList<String>, context: Context) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    val gson = Gson()
    val json = gson.toJson(list)
    editor.putString("myArrayListKey", json)
    editor.apply()
}

private fun getArrayList(context: Context): ArrayList<String> {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE)
    val gson = Gson()
    val json = sharedPreferences.getString("myArrayListKey", null)

    return if (json != null) {
        val type = object : TypeToken<ArrayList<String>>() {}.type
        gson.fromJson(json, type)
    } else {
        ArrayList()
    }
}

@Preview(showBackground = true)
@Composable
fun PantryPreview(){
    PantryChefTheme {
        PantryChefScreen()
    }
}

