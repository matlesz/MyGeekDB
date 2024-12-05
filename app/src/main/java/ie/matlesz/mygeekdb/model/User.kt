package ie.matlesz.mygeekdb.model

data class User(
        val uid: String = "",
        val email: String = "",
        val displayName: String? = null,
        // Add any other user properties you want to store
        val favoriteMovies: List<String> = emptyList(),
        val favoriteSeries: List<String> = emptyList(),
        val photoUrl: String? = null
)