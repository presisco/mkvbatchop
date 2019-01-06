package com.presisco.mkvbatchop

import com.presisco.mkvbatchop.ui.MainDialog
import tornadofx.App
import tornadofx.launch

object Entrance {
    @JvmStatic
    fun main(args: Array<String>) {
        launch<APP>()
    }

    class APP: App(MainDialog::class)
}