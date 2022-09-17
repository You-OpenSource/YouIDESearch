package com.github.youopensource.youjetbrainsearch.services

import com.github.youopensource.youjetbrainsearch.MyBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager

import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NotNull


class MyApplicationService {

    init {
        println(MyBundle.message("applicationService"))
    }
}
