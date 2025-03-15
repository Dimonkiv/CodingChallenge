package app.bettermetesttask.movies.sections.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bettermetesttask.domainmovies.entries.Movie
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieBottomSheet(
    movie: Movie?,
    onStateChange: (SheetValue) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    if (movie != null) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = { onStateChange(SheetValue.Hidden) },
        ) {
            MovieContent(movie)
        }
    }
}

@Composable
fun MovieContent(
    movie: Movie
) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = movie.posterPath,
                    contentDescription = "Movie Poster",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = movie.title, fontSize = 24.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = movie.description, fontSize = 16.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = if (movie.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like Button",
                        tint = if (movie.liked) Color.Red else Color.Gray
                    )
                }
            }
        }
}