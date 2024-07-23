package com.example.moviesapp.movieList.presentation

sealed interface MovieListUiEvents {
    data class Paginate(val category: String):MovieListUiEvents
    data object Navigate:MovieListUiEvents
}