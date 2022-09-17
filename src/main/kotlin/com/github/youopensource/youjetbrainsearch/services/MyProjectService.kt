package com.github.youopensource.youjetbrainsearch.services

import com.github.youopensource.youjetbrainsearch.MyBundle
import com.github.youopensource.youjetbrainsearch.events.DocumentChangedEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import io.reactivex.rxjava3.processors.BehaviorProcessor
import org.jetbrains.annotations.NotNull
import java.awt.EventQueue
import java.util.concurrent.TimeUnit

class MyProjectService(project: Project) {
    val documentChangeTopic: Topic<DocumentChangedEvent> =
        Topic.create("youcom.documentChanged", DocumentChangedEvent::class.java)
    val publisher: BehaviorProcessor<DocumentEvent> = BehaviorProcessor.create()

    init {
        println(MyBundle.message("projectService", project.name))

        publisher.debounce(3, TimeUnit.SECONDS).subscribe {
            ApplicationManager.getApplication().invokeLater {
                ApplicationManager.getApplication().messageBus.syncPublisher(documentChangeTopic)
                    .onDocumentChange(it)
            }
        }

        EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(@NotNull event: DocumentEvent) {
                // TODO send proper event
                publisher.onNext(event)
//                val document: Document = event.document
//                val file = FileDocumentManager.getInstance().getFile(document)
//                val offset: Int = event.offset
//                val newLength: Int = event.newLength
//
//                // actual logic depends on which line we want to call 'changed' when '\n' is inserted
//                val firstLine: Int = document.getLineNumber(offset)
//                val lastLine = if (newLength == 0) firstLine else document.getLineNumber(offset + newLength - 1)

            }
        }) { }

    }
}
