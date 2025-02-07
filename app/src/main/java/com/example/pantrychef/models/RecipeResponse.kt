import com.example.pantrychef.models.Recipe
import com.google.gson.annotations.SerializedName

data class RecipeResponse(
    @SerializedName("results") val results: List<Recipe>
)