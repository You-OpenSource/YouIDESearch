package com.github.youopensource.youjetbrainsearch.data.repository

import com.github.youopensource.youjetbrainsearch.data.CodeSuggestionApiResult
import com.github.youopensource.youjetbrainsearch.data.SolutionRequest
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RemoteYouRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://you.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val apiService: APIService = retrofit.create(APIService::class.java);

    fun getCodeSuggestions(request: SolutionRequest): Observable<CodeSuggestionApiResult> {
        if (request.codeLine == null) {
            return Observable.empty()
        }
        return Observable.create {
            val request: Call<CodeSuggestionApiResult?> = apiService.getApiResult(request.codeLine, 15, "codegrepper", 1)

            try {
                val body = request.execute().body()
                if (body != null) {
                    it.onNext(body)
                }
                it.onComplete()
            } catch (e: Exception) {
                it.onError(e)
            }
        }
    }
}
