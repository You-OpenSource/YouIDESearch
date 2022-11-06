package com.github.youopensource.youjetbrainsearch.data

import java.util.*

class YouPreferencesState {
    var onlySelectionSearch = false
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as YouPreferencesState
        return onlySelectionSearch == that.onlySelectionSearch
    }

    override fun hashCode(): Int {
        return Objects.hash(onlySelectionSearch)
    }
}
