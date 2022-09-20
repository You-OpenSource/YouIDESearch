package com.github.youopensource.youjetbrainsearch.data.repository

import com.github.youopensource.youjetbrainsearch.data.CodeSuggestionApiResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {
    //@GET("generic?service=codegrepper&version=1&fields=code_snippets%2Curl")
    @GET("generic?service=codegrepper&version=1&fields=code_snippets%2Curl")
    fun getApiResult(
        @Query("query") queryId: String?,
        @Query("size") sizeId: Int,
        @Query("source") sourceId: String?,
        @Query("page") pageId: Int
    ): Call<CodeSuggestionApiResult?>
}
