package com.github.youopensource.youjetbrainsearch.data

data class Solution(
    val number: Int,
    val codeSnipped: String?,
    val solutionText: String? = null,
    val solutionLink: String? = null
)
