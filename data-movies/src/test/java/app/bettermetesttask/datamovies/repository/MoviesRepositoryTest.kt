package app.bettermetesttask.datamovies.repository

import app.bettermetesttask.datamovies.database.entities.MovieEntity
import app.bettermetesttask.datamovies.repository.stores.MoviesLocalStore
import app.bettermetesttask.datamovies.repository.stores.MoviesMapper
import app.bettermetesttask.datamovies.repository.stores.MoviesRestStore
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domaincore.utils.connectivity.ConnectivityManager
import app.bettermetesttask.domainmovies.entries.Movie
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.lang.RuntimeException

@ExtendWith(MockitoExtension::class)
internal class MoviesRepositoryTest {

    private val localStore: MoviesLocalStore = mock()
    private val restStore: MoviesRestStore = mock()
    private val mapper: MoviesMapper = mock()
    private val connectivityManager: ConnectivityManager = mock()

    private lateinit var repository: MoviesRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = MoviesRepositoryImpl(localStore, restStore, mapper, connectivityManager)
    }

    @Test
    fun `getMovies should fetch from restStore when network is available and cache them`() = runTest {
        // Given: Network is available
        `when`(connectivityManager.isNetworkAvailable()).thenReturn(true)

        val movies = listOf(Movie(1, "Movie 1", "Desc", null, false))
        val movieEntities = listOf(MovieEntity(1, "Movie 1", "Desc", null))

        `when`(restStore.getMovies()).thenReturn(movies)
        `when`(mapper.mapToLocal(any())).thenReturn(movieEntities.first())

        // When
        val result = repository.getMovies()

        // Then
        assertTrue(result is Result.Success)
        verify(restStore).getMovies()
        verify(mapper).mapToLocal(any())
        verify(localStore).insertMovie(any())
    }

    @Test
    fun `getMovies should fetch from localStore when network is unavailable`() = runTest {
        // Given: No internet connection
        `when`(connectivityManager.isNetworkAvailable()).thenReturn(false)

        val movieEntities = listOf(MovieEntity(1, "Movie 1", "Desc", null))
        val movies = listOf(Movie(1, "Movie 1", "Desc", null, false))

        `when`(localStore.getMovies()).thenReturn(movieEntities)
        `when`(mapper.mapFromLocal(any())).thenReturn(movies.first())

        // When
        val result = repository.getMovies()

        // Then
        assertTrue(result is Result.Success)
        verify(localStore).getMovies()
        verify(mapper, times(movieEntities.size)).mapFromLocal(any())
        verifyNoInteractions(restStore)
    }

    @Test
    fun `getMovies should return error if network fails and no local data is available`() = runTest {
        // Given: Network is available but API call fails
        `when`(connectivityManager.isNetworkAvailable()).thenReturn(true)
        val errorMessage = "Network Error"
        `when`(restStore.getMovies()).thenThrow(RuntimeException(errorMessage))
        `when`(localStore.getMovies()).thenReturn(emptyList())

        // When
        val result = repository.getMovies()

        // Then
        assertTrue(result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).error.message)
        verify(localStore, never()).insertMovie(any())
    }

    @Test
    fun `getMovie should return a movie from localStore`() = runTest {
        // Given
        val movieId = 1
        val movieEntity = MovieEntity(1, "Movie 1", "Desc", null)
        val movie = Movie(1, "Movie 1", "Desc", null, false)

        `when`(localStore.getMovie(movieId)).thenReturn(movieEntity)
        `when`(mapper.mapFromLocal(movieEntity)).thenReturn(movie)

        // When
        val result = repository.getMovie(movieId)

        // Then
        assertTrue(result is Result.Success)
        verify(localStore).getMovie(movieId)
        verify(mapper).mapFromLocal(movieEntity)
    }

    @Test
    fun `observeLikedMovieIds should return a Flow of liked movie IDs`() = runTest {
        // Given
        val likedMoviesFlow = flowOf(listOf(1, 2, 3))
        `when`(localStore.observeLikedMoviesIds()).thenReturn(likedMoviesFlow)

        // When
        val result = repository.observeLikedMovieIds().first()

        // Then
        assertEquals(listOf(1, 2, 3), result)
        verify(localStore).observeLikedMoviesIds()
    }

    @Test
    fun `addMovieToFavorites should call likeMovie in localStore`() = runTest {
        // Given
        val movieId = 1

        // When
        repository.addMovieToFavorites(movieId)

        // Then
        verify(localStore).likeMovie(movieId)
    }

    @Test
    fun `removeMovieFromFavorites should call dislikeMovie in localStore`() = runTest {
        // Given
        val movieId = 1

        // When
        repository.removeMovieFromFavorites(movieId)

        // Then
        verify(localStore).dislikeMovie(movieId)
    }
}