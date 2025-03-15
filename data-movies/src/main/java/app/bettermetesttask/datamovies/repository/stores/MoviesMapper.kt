package app.bettermetesttask.datamovies.repository.stores

import app.bettermetesttask.datamovies.database.entities.MovieEntity
import app.bettermetesttask.domainmovies.entries.Movie
import javax.inject.Inject

class MoviesMapper @Inject constructor() {

    fun mapToLocal(movie: Movie) = MovieEntity(
        id = movie.id,
        title = movie.title,
        description = movie.description,
        posterPath = movie.posterPath
    )


    fun mapFromLocal(entity: MovieEntity) = Movie(
        id = entity.id,
        title = entity.title,
        description = entity.description,
        posterPath = entity.posterPath
    )
}
