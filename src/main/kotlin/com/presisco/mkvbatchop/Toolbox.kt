package com.presisco.mkvbatchop

object Toolbox{

    fun extractFilename(path: String) = path.substringAfterLast("\\")

}