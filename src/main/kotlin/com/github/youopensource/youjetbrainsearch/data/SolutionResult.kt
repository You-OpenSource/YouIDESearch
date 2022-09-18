package com.github.youopensource.youjetbrainsearch.data

data class SolutionResult(
    val loading: Boolean = false,
    val solutions: List<Solution>? = null
)
