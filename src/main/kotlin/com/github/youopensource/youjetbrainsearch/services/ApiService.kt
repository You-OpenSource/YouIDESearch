package com.github.youopensource.youjetbrainsearch.services

import com.github.youopensource.youjetbrainsearch.data.Solution
import com.github.youopensource.youjetbrainsearch.data.SolutionRequest
import com.github.youopensource.youjetbrainsearch.data.SolutionResult
import com.github.youopensource.youjetbrainsearch.data.repository.RemoteYouRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import java.util.concurrent.TimeUnit

object ApiService {
    private val publisher: BehaviorProcessor<SolutionResult> = BehaviorProcessor.create()
    private val requestPublisher: BehaviorProcessor<SolutionRequest> = BehaviorProcessor.create()

    init {
        requestPublisher.debounce(1, TimeUnit.SECONDS).subscribe({
            if (it.codeLine.isNullOrBlank()) {
                println("Skipping empty line")
                return@subscribe
            }
            try {
                val firstResult = RemoteYouRepository.getCodeSuggestions(it).blockingFirst()
                val results = firstResult.searchResults!!.results!!.flatMap {
                    it.codeSnippets!!
                }.mapIndexed { id, result ->
                    Solution(id, result.snippetCode)
                }
                publisher.onNext(
                    SolutionResult(
                        solutions = results
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                println("oops")
            }
        }, {
            publisher.onError(it)
        })

    }

    public fun requestSolution(solutionRequest: SolutionRequest) {
        requestPublisher.onNext(solutionRequest)
    }

    public fun getRequestPublisher() = requestPublisher
    public fun getSolutionObservable(): Observable<SolutionResult> = publisher.toObservable()


}
