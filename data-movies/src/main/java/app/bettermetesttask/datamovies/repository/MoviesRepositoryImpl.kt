package app.bettermetesttask.datamovies.repository

import app.bettermetesttask.datamovies.repository.stores.MoviesLocalStore
import app.bettermetesttask.datamovies.repository.stores.MoviesMapper
import app.bettermetesttask.datamovies.repository.stores.MoviesRestStore
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domaincore.utils.connectivity.ConnectivityManager
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MoviesRepositoryImpl @Inject constructor(
    private val localStore: MoviesLocalStore,
    private val restStore: MoviesRestStore,
    private val mapper: MoviesMapper,
    private val connectivityManager: ConnectivityManager
) : MoviesRepository {

    override suspend fun getMovies(): Result<List<Movie>> {
        return Result.of {
            if (connectivityManager.isNetworkAvailable()) {
                getAndCacheMovies()
            } else {
                getMoviesFromLocal()
            }
        }
    }

    override suspend fun getMovie(id: Int): Result<Movie> {
        return Result.of { mapper.mapFromLocal(localStore.getMovie(id)) }
    }

    override fun observeLikedMovieIds(): Flow<List<Int>> {
        return localStore.observeLikedMoviesIds()
    }

    override suspend fun addMovieToFavorites(movieId: Int) {
        localStore.likeMovie(movieId)
    }

    override suspend fun removeMovieFromFavorites(movieId: Int) {
        localStore.dislikeMovie(movieId)
    }

    private suspend fun getAndCacheMovies(): List<Movie> {
        return restStore.getMovies().map { movie ->
            localStore.insertMovie(mapper.mapToLocal(movie))
            return@map movie
        }
    }

    private suspend fun getMoviesFromLocal(): List<Movie> {
        return localStore.getMovies().map { movieEntity ->
            mapper.mapFromLocal(movieEntity)
        }
    }
}