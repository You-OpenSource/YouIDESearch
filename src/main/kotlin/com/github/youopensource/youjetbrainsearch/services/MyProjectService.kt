package com.github.youopensource.youjetbrainsearch.services

import com.github.youopensource.youjetbrainsearch.MyBundle
import com.github.youopensource.youjetbrainsearch.data.SolutionRequest
import com.github.youopensource.youjetbrainsearch.events.DocumentChangedEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.wm.ToolWindowFactoryEx
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.messages.Topic
import io.reactivex.rxjava3.processors.BehaviorProcessor
import java.util.concurrent.TimeUnit

class MyProjectService(project: Project) {
    val documentChangeTopic: Topic<DocumentChangedEvent> =
        Topic.create("youcom.documentChanged", DocumentChangedEvent::class.java)
    val publisher: BehaviorProcessor<CaretEvent> = BehaviorProcessor.create()

    init {
        println(MyBundle.message("projectService", project.name))

        publisher.debounce(1, TimeUnit.SECONDS).subscribe {
            ApplicationManager.getApplication().invokeLater {
                ApplicationManager.getApplication().messageBus.syncPublisher(documentChangeTopic)
                    .onDocumentChange(it)
            }
        }

        EditorFactory.getInstance().eventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("You.com")
                val editor = event.editor
                if (toolWindow?.isVisible == false || editor.editorKind != EditorKind.MAIN_EDITOR) {
                    return
                }
                val line = event.caret!!.visualPosition.line
                val start = editor.document.getLineStartOffset(line)
                val end = editor.document.getLineEndOffset(line)
                val text = editor.document.getText(TextRange.create(start, end))
                ApiService.getRequestPublisher().onNext(
                    SolutionRequest(
                        text
                    )
                )
                publisher.onNext(event)
            }
        }) { }

    }
}
