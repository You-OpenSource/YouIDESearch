package com.github.youopensource.youjetbrainsearch.screen

import com.github.youopensource.youjetbrainsearch.data.Solution
import com.github.youopensource.youjetbrainsearch.services.ApiService
import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
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
import java.awt.Desktop
import java.net.URI
import javax.swing.JButton

class SideSuggestionViewFactory : ToolWindowFactory {

    private val allButtons: ArrayList<SmallButton> = arrayListOf();
    private val allEditors: ArrayList<EditorTextField> = arrayListOf();
    private var dataProviderPanel: DataProviderPanel? = null
    private var project: Project? = null
    private var lastChange: DocumentEvent? = null

    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool window
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        println("${toolWindow.id} is this id");
        this.project = project

        val contentFactory = ContentFactory.SERVICE.getInstance()

        dataProviderPanel = DataProviderPanel().apply {
            setFocusCycleRoot(true)
            setBorder(JBUI.Borders.empty(5, 10, 10, 15))
        }
        dataProviderPanel!!.add(
            JBLabel("Loading...")
        )

        val jbScrollPane = JBScrollPane(dataProviderPanel, 20, 31);
        val content = contentFactory.createContent(jbScrollPane, "", false)
        toolWindow.contentManager.addContent(content)
        val disposable = ApiService.getSolutionObservable().subscribe({
            ApplicationManager.getApplication().invokeLater {
                if (!it.loading) {
                    onSuggestion(it.solutions!!)
                } else {
                    println("Loading...")
                }
            }
        }, {
            ApplicationManager.getApplication().invokeLater {
                onError(it)
            }
        }
        )
    }

    private fun onSuggestion(solutionList: List<Solution>) {
        cleanLayout()
        if(solutionList.isEmpty()) {
            return
        }
        solutionList.map { solution ->
            val elements = createCodeSuggestionView(project!!, solution)
            elements.button.apply {
                addActionListener {
                    ApiService.recordButtonClickedEvent(solution)
                    WriteCommandAction.runWriteCommandAction(
                        project
                    ) {
                        val editor = FileEditorManager.getInstance(project!!).selectedTextEditor!!
                        val caret = editor.caretModel.primaryCaret
                        var start = caret.selectionStart
                        var end = caret.selectionEnd
                        if(start == end) {
                            start = caret.visualLineStart
                            end = caret.visualLineEnd
                        }
                        val document = editor.document
                        document.deleteString(start, end)
                        document.insertString(start, solution.codeSnippet!!)
                    }
                }

            }
            allEditors.add(elements.field)
            dataProviderPanel?.add(
                elements.panel
            )
            dataProviderPanel?.add(elements.field)
        }


    }

    private fun cleanLayout() {
        dataProviderPanel!!.removeAll()
        allButtons.clear()
        allEditors.clear()
    }

    private fun onError(throwable: Throwable) {
        cleanLayout()
        dataProviderPanel?.add(
            Centerizer(JBLabel("Uh oh"))
        )
    }


    private fun createCodeSuggestionView(project: Project, solution: Solution): SuggestionPanel {
        val smallButton = SmallButton("Try Solution ${solution.number}")
        val horizontalPanel = JBPanelWithEmptyText(
            HorizontalLayout(5)
        )
        horizontalPanel.add(smallButton)
        if(solution.solutionLink != null) {
            val jbLabel = JButton("Open in browser", AllIcons.Nodes.PpWeb)
            jbLabel.addActionListener {
                Desktop.getDesktop().browse(URI(solution.solutionLink));
            }
            horizontalPanel.add(jbLabel)
        }
        val editorTextField = editorTextField(project, solution.codeSnippet!!)
        return SuggestionPanel(smallButton, horizontalPanel, editorTextField)
    }

    private fun editorTextField(project: Project, text: String): EditorTextField {
        val document = FileEditorManager.getInstance(project).selectedTextEditor!!.document
        val language = PsiDocumentManager.getInstance(project).getPsiFile(document)?.language ?: Language.findLanguageByID("Python")!!
        val editorField = EditorTextFieldProvider.getInstance().getEditorField(
            language, project, listOf(
                EditorCustomization {
                    val editorSettings: EditorSettings = it.getSettings()
                    editorSettings.isLineNumbersShown = false
                    editorSettings.isLineMarkerAreaShown = false
                    editorSettings.isAutoCodeFoldingEnabled = false
                    editorSettings.isUseSoftWraps = true
                    editorSettings.isAnimatedScrolling = false
                    editorSettings.isBlinkCaret = false
                    editorSettings.isRightMarginShown = false
                    editorSettings.isShowIntentionBulb = false
                    it.setViewer(true)
                    it.setCaretVisible(false)
                    it.setCaretEnabled(false)
                    it.putUserData(IncrementalFindAction.SEARCH_DISABLED, true)
                    it.setRendererMode(false)
                    val scheme = EditorColorsManager.getInstance().schemeForCurrentUITheme
                    val c = scheme.getColor(EditorColors.READONLY_BACKGROUND_COLOR)
                    it.setBackgroundColor(c ?: scheme.defaultBackground)
                    it.setColorsScheme(scheme)
                }
            ))
        editorField.document = PsiManager.getInstance(project).findViewProvider(
            LightVirtualFile(
                "test", language, text
            )
        )!!.document
        return editorField
    }

    fun dispose() {

    }
}


data class SuggestionPanel(
val button: SmallButton,
val panel: JBPanelWithEmptyText,
val field: EditorTextField
)
