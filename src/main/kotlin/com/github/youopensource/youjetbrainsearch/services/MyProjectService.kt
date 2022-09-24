package com.github.youopensource.youjetbrainsearch.services

import com.github.youopensource.youjetbrainsearch.MyBundle
import com.github.youopensource.youjetbrainsearch.data.SolutionRequest
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiDocumentManager
import io.reactivex.rxjava3.processors.BehaviorProcessor

class MyProjectService(project: Project) {
    val publisher: BehaviorProcessor<CaretEvent> = BehaviorProcessor.create()

    init {
        println(MyBundle.message("projectService", project.name))

        EditorFactory.getInstance().eventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("You.com")
                val editor = event.editor
                if (toolWindow?.isVisible == false || editor.editorKind != EditorKind.MAIN_EDITOR) {
                    return
                }
                val line = event.caret!!.visualPosition.line
                var searchText: String
                if(event.caret!!.selectedText != null) {
                    searchText = event.caret!!.selectedText!!
                } else {
                    val start = event.caret!!.visualLineStart
                    val end = event.caret!!.visualLineEnd
                    searchText = editor.document.getText(TextRange.create(start, end))
                }
                val hashComment = searchText.trim().startsWith("#")
                val javaComment = searchText.trim().startsWith("//")
                if (hashComment || javaComment) {
                    if (hashComment) {
                        searchText = searchText.trim().substring(1)
                    } else if (javaComment) {
                        searchText = searchText.trim().substring(2)
                    }

                }
                val languageSpecifier = Regex("^(?i)(java|python).*$")
                if (!searchText.trim().matches(languageSpecifier)) {
                    val language =
                        PsiDocumentManager.getInstance(project).getPsiFile(editor.document)?.language?.id?.toLowerCase()
                            ?: "python"
                    searchText = "$language ${searchText.trim()}"

                }
                ApiService.getRequestPublisher().onNext(
                    SolutionRequest(
                        searchText
                    )
                )
                publisher.onNext(event)
            }
        }) { }

    }
}
