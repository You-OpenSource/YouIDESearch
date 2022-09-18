package com.github.youopensource.youjetbrainsearch.events

import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.DocumentEvent

interface DocumentChangedEvent {

    fun onDocumentChange(event: CaretEvent)
}
