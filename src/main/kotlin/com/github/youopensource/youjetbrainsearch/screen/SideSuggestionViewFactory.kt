package com.github.youopensource.youjetbrainsearch.screen

import com.github.youopensource.youjetbrainsearch.events.DocumentChangedEvent
import com.github.youopensource.youjetbrainsearch.services.MyProjectService
import com.intellij.application.subscribe
import com.intellij.json.JsonFileType
import com.intellij.json.JsonLanguage
import com.intellij.openapi.Disposable
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorSettings
import com.intellij.openapi.editor.actions.IncrementalFindAction
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.EditorCustomization
import com.intellij.ui.EditorTextField
import com.intellij.ui.EditorTextFieldProvider
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import org.jetbrains.annotations.NotNull

class SideSuggestionViewFactory : ToolWindowFactory, Disposable {

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
        val service = project.service<MyProjectService>()
        service.documentChangeTopic.subscribe(this, object : DocumentChangedEvent {
            override fun onDocumentChange(event: DocumentEvent) {
                if (lastChange == null) {
                    lastChange = event
                } else {
                    onSuggestion()
                }
            }
        })
    }

    private fun onSuggestion() {
        dataProviderPanel!!.removeAll()
        listOf(
            "import pandas as pd",
            "import postgresql",
            "import pandas as pd\ndf = pd.read_csv('file_name')\ndf['key3'] = df['key1']+df['key3']",
            "import pandas as pd",
            "import postgresql",
            "import pandas as pd\ndf = pd.read_csv('file_name')\ndf['key3'] = df['key1']+df['key3']",
            "import pandas as pd",
            "import postgresql",
            "import pandas as pd\ndf = pd.read_csv('file_name')\ndf['key3'] = df['key1']+df['key3']",
            "import pandas as pd",
            "import postgresql",
            "import pandas as pd\ndf = pd.read_csv('file_name')\ndf['key3'] = df['key1']+df['key3']",
        ).map { text ->
            val elements = createCodeSuggestionView(project!!, text)
            allButtons.add(elements.first.apply {
                addActionListener {
                    WriteCommandAction.runWriteCommandAction(
                        project
                    ) {
                        // TODO add logic to handle current location
                        val editor = FileEditorManager.getInstance(project!!).selectedTextEditor!!
                        val document = editor.document
                        document.deleteString(0, document.textLength)
                        document.insertString(0, text)
                    }
                }

            })
            allEditors.add(elements.second)
            dataProviderPanel?.add(
                elements.first
            )
            dataProviderPanel?.add(elements.second)
        }


    }


    private fun createCodeSuggestionView(project: Project, text: String): Pair<SmallButton, EditorTextField> {
        val smallButton = SmallButton("Try Solution 1")
        val editorTextField = editorTextField(project, text)
        return Pair(smallButton, editorTextField)
    }

    private fun editorTextField(project: Project, text: String): @NotNull EditorTextField {
        val editorField = EditorTextFieldProvider.getInstance().getEditorField(
            JsonLanguage.INSTANCE, project, listOf(
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
                    //                it.setPrefixTextAndAttributes(
                    //                    this.editorRequest.getCurrentLinePrefix(),
                    //                    scheme.getAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT)
                    //                )
                }
            ))
        editorField.document = PsiManager.getInstance(project).findViewProvider(
            LightVirtualFile(
                "test", JsonFileType.INSTANCE, text
            )
        )!!.document
        return editorField
    }

    override fun dispose() {

    }
}
