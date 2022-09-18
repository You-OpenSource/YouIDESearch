package com.github.youopensource.youjetbrainsearch.services

import com.github.youopensource.youjetbrainsearch.data.Solution
import com.github.youopensource.youjetbrainsearch.data.SolutionRequest
import com.github.youopensource.youjetbrainsearch.data.SolutionResult
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import java.util.concurrent.TimeUnit

object ApiService {
    private val publisher: BehaviorProcessor<SolutionResult> = BehaviorProcessor.create()
    private val requestPublisher: BehaviorProcessor<SolutionRequest> = BehaviorProcessor.create()

    init {
        requestPublisher.debounce(1, TimeUnit.SECONDS).subscribe {
            if (it.codeLine.isNullOrBlank()) {
                println("Skipping empty line")
                return@subscribe
            }
            publisher.onNext(
                SolutionResult(
                    solutions = listOf(
                        Solution(1, "${it.codeLine} -> 0"),
                        Solution(2, "${it.codeLine} -> 1"),
                        Solution(3, "${it.codeLine} -> 2"),
                        Solution(4, "${it.codeLine} -> 3"),
                        Solution(5, "${it.codeLine} -> 4"),
                        Solution(6, "${it.codeLine} -> 5"),
                        Solution(7, "${it.codeLine} -> 6"),
                        Solution(8, "${it.codeLine} -> 7"),
                        Solution(9, "${it.codeLine} -> 8"),
                        Solution(10, "${it.codeLine} -> 9"),
                        Solution(11, "${it.codeLine} -> 10"),
                        Solution(12, "${it.codeLine} -> 11"),
                    )
                )
            )
        }

    }

    public fun requestSolution(solutionRequest: SolutionRequest) {
        requestPublisher.onNext(solutionRequest)
    }

    public fun getRequestPublisher() = requestPublisher
    public fun getSolutionObservable(): Observable<SolutionResult> = publisher.toObservable()


}
