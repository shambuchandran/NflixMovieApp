package com.example.moviesapp.movieList.data.local.movies

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface MovieDao {
    @Upsert
    suspend fun upsertMovieList(movieList: List<MovieEntity>)

    @Query("SELECT * FROM movieentity WHERE id = :id")
    suspend fun getMovieById(id:Int):MovieEntity

    @Query("SELECT * FROM MovieEntity WHERE category= :category")
    suspend fun getMovieListByCategory(category: String):List<MovieEntity>
    @Query("SELECT lastFetched FROM MovieEntity WHERE category = :category LIMIT 2")
    suspend fun getLastFetchedTime(category: String):Long?
}