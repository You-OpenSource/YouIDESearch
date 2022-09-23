package com.github.youopensource.youjetbrainsearch.data.repository

import com.github.youopensource.youjetbrainsearch.data.*
import com.github.youopensource.youjetbrainsearch.services.TelemetryService
import com.intellij.structuralsearch.plugin.ui.ConfigurationManager
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.concurrent.thread

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
            thread {
                apiService.recordAnalyticsEvent(
                    AnalyticsEvent(
                        "intellij_user_search",
                        EventData(
                            request.codeLine
                        ),
                        DeviceProperties(
                            0, 0, 0, 0, true
                        )
                    ),
                ).execute()
                println("Analytics executed")
            }

            val request: Call<CodeSuggestionApiResult?> = apiService.getApiResult(request.codeLine, 15, "codesnippets", 1)

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

    fun sendButtonClickedEvent(buttonTitle: String) {
        thread {
            TelemetryService.instance.action("intellij_user_click")
                .apply {
                    //TODO more params for users
                }
                .send();
            apiService.recordAnalyticsEvent(
                AnalyticsEvent(
                    "intellij_user_click",
                    EventData(
                        buttonTitle
                    ),
                    DeviceProperties(
                        0, 0, 0, 0, true
                    )
                ),
            ).execute()
            println("Analytics executed")
        }
    }
}
