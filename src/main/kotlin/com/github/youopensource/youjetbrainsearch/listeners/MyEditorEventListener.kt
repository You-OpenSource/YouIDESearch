package com.github.youopensource.youjetbrainsearch.listeners

import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.EditorEventListener

internal class MyEditorEventListener : DocumentListener {

    override fun documentChanged(event: DocumentEvent) {
        print(event.isWholeTextReplaced)
        super.documentChanged(event)
    }
}
