package com.example.moviesapp.movieList.data.remote.response

import androidx.room.Database
import androidx.room.Entity
import androidx.room.RoomDatabase
import com.example.moviesapp.movieList.data.local.movies.MovieDao
import com.example.moviesapp.movieList.data.local.movies.MovieEntity

@Database(entities = [MovieEntity::class], version = 1, exportSchema = false)
abstract class MovieDataBase:RoomDatabase() {
    abstract val movieDao:MovieDao
}