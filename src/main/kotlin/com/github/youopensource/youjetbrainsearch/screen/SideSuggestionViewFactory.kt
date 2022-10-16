package com.github.youopensource.youjetbrainsearch.screen

import com.github.youopensource.youjetbrainsearch.data.Solution
import com.github.youopensource.youjetbrainsearch.services.ApiService
import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorSettings
import com.intellij.openapi.editor.actions.IncrementalFindAction
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.EditorCustomization
import com.intellij.ui.EditorTextField
import com.intellij.ui.EditorTextFieldProvider
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.panels.HorizontalLayout
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.Centerizer
import com.intellij.util.ui.JBUI
import io.reactivex.rxjava3.disposables.Disposable
import java.awt.Desktop
import java.net.URI
import javax.swing.JButton

class SideSuggestionViewFactory : ToolWindowFactory {
    private val LOG: Logger = Logger.getInstance(this.javaClass)
    private var dataProviderPanel: DataProviderPanel? = null
    private var project: Project? = null
    private var disposable: Disposable? = null

    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool window
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        LOG.debug("Creating toolwindow with id ${toolWindow.id}")
        this.project = project
        disposable?.dispose()
        val contentFactory = ContentFactory.SERVICE.getInstance()

        dataProviderPanel = DataProviderPanel().apply {
            isFocusCycleRoot = true
            border = JBUI.Borders.empty(5, 10, 10, 15)
        }
        dataProviderPanel!!.add(
            JBLabel("Loading...")
        )

        val jbScrollPane = JBScrollPane(dataProviderPanel, 20, 31)
        val content = contentFactory.createContent(jbScrollPane, "", false)
        toolWindow.contentManager.addContent(content)
        disposable = ApiService.getSolutionObservable().subscribe({
            if (project.isDisposed) {
                return@subscribe
            }
            ApplicationManager.getApplication().invokeLater {
                onSuggestion(it.solutions!!)
            }
        }, {
            ApplicationManager.getApplication().invokeLater {
                onError(it)
            }
        })
    }


    private fun onSuggestion(solutionList: List<Solution>) {
        cleanLayout()
        if (solutionList.isEmpty()) {
            LOG.debug("No solutions were found, skipping UI")
            return
        }
        solutionList.map { solution ->
            val elements = createCodeSuggestionView(project!!, solution)
            dataProviderPanel?.add(
                elements.panel
            )
            dataProviderPanel?.add(elements.field)
        }
    }

    private fun cleanLayout() {
        LOG.debug("Cleaning layout")
        dataProviderPanel!!.removeAll()
    }

    private fun onError(throwable: Throwable) {
        LOG.error(throwable)
        cleanLayout()
        dataProviderPanel?.add(
            Centerizer(JBLabel("Error occurred"))
        )
    }


    private fun createCodeSuggestionView(project: Project, solution: Solution): SuggestionPanel {
        val smallButton = SmallButton("Try Solution ${solution.number}").apply {
            addActionListener {
                onButtonClicked(solution, project)
            }
        }
        val horizontalPanel = JBPanelWithEmptyText(HorizontalLayout(5)).apply {
            add(smallButton)
        }
        if (solution.solutionLink != null) {
            val jbLabel = JButton("Open in browser", AllIcons.Nodes.PpWeb)
            jbLabel.addActionListener {
                Desktop.getDesktop().browse(URI(solution.solutionLink))
            }
            horizontalPanel.add(jbLabel)
        }
        val editorTextField = editorTextField(project, solution.codeSnippet!!)
        return SuggestionPanel(smallButton, horizontalPanel, editorTextField)
    }

    private fun onButtonClicked(
        solution: Solution,
        project: Project
    ) {
        ApiService.recordButtonClickedEvent(solution)
        WriteCommandAction.runWriteCommandAction(
            project
        ) {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
            val caret = editor.caretModel.primaryCaret
            var start = caret.selectionStart
            var end = caret.selectionEnd
            if (start == end) {
                start = caret.visualLineStart
                end = caret.visualLineEnd
            }
            val document = editor.document
            document.deleteString(start, end)
            document.insertString(start, solution.codeSnippet!!)
        }
    }

    private fun editorTextField(project: Project, text: String): EditorTextField {
        val document = FileEditorManager.getInstance(project).selectedTextEditor!!.document
        val language = PsiDocumentManager.getInstance(project).getPsiFile(document)?.language
            ?: Language.findLanguageByID("Python")!!
        val editorField = EditorTextFieldProvider.getInstance().getEditorField(
            language, project, listOf(
                EditorCustomization {
                    val editorSettings: EditorSettings = it.settings
                    it.isViewer = true
                    it.isRendererMode = false
                    it.setCaretVisible(false)
                    it.setCaretEnabled(false)
                    val scheme = EditorColorsManager.getInstance().schemeForCurrentUITheme
                    val c = scheme.getColor(EditorColors.READONLY_BACKGROUND_COLOR)
                    editorSettings.isLineNumbersShown = false
                    editorSettings.isLineMarkerAreaShown = false
                    editorSettings.isAutoCodeFoldingEnabled = false
                    editorSettings.isUseSoftWraps = true
                    editorSettings.isAnimatedScrolling = false
                    editorSettings.isBlinkCaret = false
                    editorSettings.isRightMarginShown = false
                    editorSettings.isShowIntentionBulb = false
                    it.putUserData(IncrementalFindAction.SEARCH_DISABLED, true)
                    it.backgroundColor = c ?: scheme.defaultBackground
                    it.colorsScheme = scheme
                }
            ))
        editorField.document = PsiManager.getInstance(project).findViewProvider(
            LightVirtualFile(
                "ignored", language, text
            )
        )!!.document
        return editorField
    }

    fun dispose() {

    }
}
