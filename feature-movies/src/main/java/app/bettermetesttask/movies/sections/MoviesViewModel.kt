package app.bettermetesttask.movies.sections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.interactors.AddMovieToFavoritesUseCase
import app.bettermetesttask.domainmovies.interactors.ObserveMoviesUseCase
import app.bettermetesttask.domainmovies.interactors.RemoveMovieFromFavoritesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MoviesViewModel @Inject constructor(
    private val observeMoviesUseCase: ObserveMoviesUseCase,
    private val likeMovieUseCase: AddMovieToFavoritesUseCase,
    private val dislikeMovieUseCase: RemoveMovieFromFavoritesUseCase,
) : ViewModel() {

    private val moviesMutableFlow: MutableStateFlow<MoviesState> = MutableStateFlow(MoviesState.Initial)
    private val currentLoadedState: MoviesState.Loaded
        get() = (moviesStateFlow.value as? MoviesState.Loaded) ?: MoviesState.Loaded()

    val moviesStateFlow: StateFlow<MoviesState>
        get() = moviesMutableFlow.asStateFlow()

    fun loadMovies() {
        viewModelScope.launch {
            moviesMutableFlow.emit(MoviesState.Loading)
            observeMoviesUseCase.get()
                .collect { result ->
                    when(result) {
                        is Result.Success ->
                            moviesMutableFlow.emit(MoviesState.Loaded(result.data))

                        is Result.Error ->
                            moviesMutableFlow.emit(MoviesState.Error("There is something wrong"))
                    }
                }
        }
    }

    fun likeMovie(movie: Movie) {
        viewModelScope.launch {
            if (!movie.liked) {
                likeMovieUseCase.get(movie.id)
            } else {
                dislikeMovieUseCase.get(movie.id)
            }

            updateMovieState(movie.id, movie.liked)
        }
    }

    private fun updateMovieState(movieId: Int, liked: Boolean) {
        val currentState = moviesMutableFlow.value
        if (currentState is MoviesState.Loaded) {
            val updatedMovies = currentState.movies.map {
                if (it.id == movieId) {
                    it.copy(liked = liked)
                } else {
                    it
                }
            }
            moviesMutableFlow.value = currentLoadedState.copy(movies = updatedMovies)
        }
    }

    fun openMovieDetails(movie: Movie) {
        moviesMutableFlow.value = currentLoadedState.copy(selectedMovie = movie)
    }

    fun closeBottomSheet(movie: Movie) {
        likeMovie(movie)
        moviesMutableFlow.value = currentLoadedState.copy(selectedMovie = null)
    }
}