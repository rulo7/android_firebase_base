package com.racobos.manhattan.main

import android.view.View
import org.jetbrains.anko.*

class MainActivityUI : AnkoComponent<MainActivity> {
    override fun createView(ui: AnkoContext<MainActivity>): View = with(ui) {
        verticalLayout {
            textView {
                text = "RAUL"
            }.lparams(width = matchParent, height = dip(28))
        }
    }
}