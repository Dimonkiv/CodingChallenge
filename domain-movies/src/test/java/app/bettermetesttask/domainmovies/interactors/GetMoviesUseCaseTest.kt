package app.bettermetesttask.domainmovies.interactors

import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.repository.MoviesRepository
import app.bettermetesttask.domaincore.utils.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class GetMoviesUseCaseTest {

    private val repository: MoviesRepository = mock()
    private lateinit var useCase: ObserveMoviesUseCase

    @BeforeEach
    fun setUp() {
        useCase = ObserveMoviesUseCase(repository)
    }

    @Test
    fun `invoke should return Success with liked movies when getMovies is Success`() = runTest {
        // Given
        val movies = listOf(
            Movie(1, "Movie 1", "Desc", null, false),
            Movie(2, "Movie 2", "Desc", null, false)
        )
        val likedMovieIds = listOf(1)
        val expectedMovies = listOf(
            Movie(1, "Movie 1", "Desc", null, true),
            Movie(2, "Movie 2", "Desc", null, false)
        )
        `when`(repository.getMovies()).thenReturn(Result.Success(movies))
        `when`(repository.observeLikedMovieIds()).thenReturn(flowOf(likedMovieIds))

        // When
        val result = useCase().first()

        // Then
        assertEquals(Result.Success(expectedMovies), result)
    }

    @Test
    fun `invoke should return Error when getMovies is Error`() = runTest {
        // Given
        val errorMessage = "Network Error"
        val exception = RuntimeException(errorMessage)
        `when`(repository.getMovies()).thenReturn(Result.Error(exception))

        // When
        val result = useCase().first()

        // Then
        assertEquals(Result.Error(exception), result)
    }

    @Test
    fun `invoke should return Success with empty liked movie IDs`() = runTest {
        // Given
        val movies = listOf(
            Movie(1, "Movie 1", "Desc", null, false),
            Movie(2, "Movie 2", "Desc", null, false)
        )
        val likedMovieIds = emptyList<Int>()
        val expectedMovies = movies.map{it.copy(liked = false)}
        `when`(repository.getMovies()).thenReturn(Result.Success(movies))
        `when`(repository.observeLikedMovieIds()).thenReturn(flowOf(likedMovieIds))

        // When
        val result = useCase().first()

        // Then
        assertEquals(Result.Success(expectedMovies), result)
    }

    @Test
    fun `invoke should return Success when no movies are returned`() = runTest {
        // Given
        val movies = emptyList<Movie>()
        val likedMovieIds = listOf(1,2,3)
        val expectedMovies = emptyList<Movie>()
        `when`(repository.getMovies()).thenReturn(Result.Success(movies))
        `when`(repository.observeLikedMovieIds()).thenReturn(flowOf(likedMovieIds))

        // When
        val result = useCase().first()

        // Then
        assertEquals(Result.Success(expectedMovies), result)
    }
}