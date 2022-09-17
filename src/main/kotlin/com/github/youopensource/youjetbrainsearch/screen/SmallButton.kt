package com.github.youopensource.youjetbrainsearch.screen

import javax.swing.JButton
import java.awt.Insets
import com.intellij.util.ui.JBUI

internal class SmallButton(label: String) : JButton(label) {
    override fun getInsets(insets: Insets): Insets {
        return insets
    }

    override fun getInsets(): Insets {
        return JBUI.emptyInsets()
    }

    init {
        font = JBUI.Fonts.smallFont()
    }
}
