package com.example.moviesapp.movieList.details

import com.example.moviesapp.movieList.domain.model.Movie

data class DetailsState(val isLoading:Boolean=false,val movie: Movie?=null)
