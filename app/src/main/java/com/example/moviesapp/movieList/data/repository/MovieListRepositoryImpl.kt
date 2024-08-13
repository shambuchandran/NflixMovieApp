package com.example.moviesapp.movieList.data.repository

import android.util.Log
import com.example.moviesapp.movieList.data.mappers.toMovie
import com.example.moviesapp.movieList.data.mappers.toMovieEntity
import com.example.moviesapp.movieList.data.remote.MovieApi
import com.example.moviesapp.movieList.data.remote.response.MovieDataBase
import com.example.moviesapp.movieList.data.remote.response.MovieDto
import com.example.moviesapp.movieList.domain.model.Movie
import com.example.moviesapp.movieList.domain.repository.MovieListRepository
import com.example.moviesapp.movieList.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MovieListRepositoryImpl @Inject constructor(
    private val movieApi: MovieApi,
    private val movieDatabase: MovieDataBase
) : MovieListRepository {
    private val cacheDurationMinutes= 30
    override suspend fun getMovieList(
        forceFetchFromRemote: Boolean,
        category: String,
        page: Int
    ): Flow<Resource<List<Movie>>> {
        return flow {
            emit(Resource.Loading(true))
            val localMovieList = movieDatabase.movieDao.getMovieListByCategory(category)
            var lastFetched= movieDatabase.movieDao.getLastFetchedTime(category)
            val isCachedValid=lastFetched != null && System.currentTimeMillis() - lastFetched < TimeUnit.MINUTES.toMillis(
                cacheDurationMinutes.toLong()
            )
            val shouldLoadLocalMovie = localMovieList.isNotEmpty() && isCachedValid && !forceFetchFromRemote
            if (shouldLoadLocalMovie) {
                emit(Resource.Success(data = localMovieList.map { movieEntity ->
                    movieEntity.toMovie(category)
                }
                ))
                emit(Resource.Loading(false))
                return@flow
            }
            val movieListFromApi = try {
                movieApi.getMoviesList(category, page)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("MovieRepository", "Network error loading movies")
                emit(Resource.Error(message = "Error loading movies"))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                Log.e("MovieRepository", "HTTP error loading movies")
                emit(Resource.Error(message = "Error loading movies"))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MovieRepository", "General error loading movies")
                emit(Resource.Error(message = "Error loading movies"))
                return@flow
            }
//            val movieEntity = movieListFromApi.results.let {
//                it.map { movieDto ->
//                    movieDto.toMovieEntity(category)
//                }
//            }
            val movieEntity = movieListFromApi.results.map {
                it.toMovieEntity(category).apply {
                    lastFetched=System.currentTimeMillis()
                }
            }
            movieDatabase.movieDao.upsertMovieList(movieEntity)
            emit(Resource.Success(movieEntity.map {
                it.toMovie(category)
            }))
            emit(Resource.Loading(false))
        }

    }

    override suspend fun getMovie(id: Int): Flow<Resource<Movie>> {
        return flow {
            emit(Resource.Loading(true))
            val movieEntity=movieDatabase.movieDao.getMovieById(id)
            if (movieEntity != null){
                emit(Resource.Success(movieEntity.toMovie(movieEntity.category)))
                emit(Resource.Loading(false))
                return@flow
            }
            emit(Resource.Error("Error no such movie"))
            emit(Resource.Loading(false))
        }
    }
}