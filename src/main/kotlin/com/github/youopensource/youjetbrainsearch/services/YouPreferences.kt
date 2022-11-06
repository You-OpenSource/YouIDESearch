package com.github.youopensource.youjetbrainsearch.services

import com.github.youopensource.youjetbrainsearch.data.YouPreferencesState
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "YouPreferences", storages = [Storage("youcom.xml")])
class YouPreferences : PersistentStateComponent<YouPreferencesState> {
    var youState: YouPreferencesState = YouPreferencesState()
    override fun getState(): YouPreferencesState {
        return youState
    }

    override fun loadState(state: YouPreferencesState) {
        XmlSerializerUtil.copyBean(state, this.youState)
    }

    companion object {
        val instance: YouPreferences
            get() = ApplicationManager.getApplication().getService(YouPreferences::class.java)
    }
}
