import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class Movie(
        @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
        @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
        @get:PropertyName("overview") @set:PropertyName("overview") var overview: String = "",
        @get:PropertyName("posterPath") @set:PropertyName("posterPath") var posterPath: String = "",
        @get:PropertyName("thumbsUp") @set:PropertyName("thumbsUp") var thumbsUp: Int = 0,
        @get:PropertyName("voteAverage")
        @set:PropertyName("voteAverage")
        var voteAverage: Double = 0.0,
        @get:PropertyName("voteCount") @set:PropertyName("voteCount") var voteCount: Int = 0,
        @get:PropertyName("popularity")
        @set:PropertyName("popularity")
        var popularity: Double = 0.0,
        @get:PropertyName("isFavorite")
        @set:PropertyName("isFavorite")
        var isFavorite: Boolean = false
) {
  // Required empty constructor for Firestore
  constructor() : this("", "", "", "", 0, 0.0, 0, 0.0, false)
}
