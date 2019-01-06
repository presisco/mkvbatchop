package com.presisco.mkvbatchop.model

import com.presisco.mkvbatchop.Toolbox

data class Task(
        var path: String,
        val containers: ArrayList<Container>
){
    override fun toString() = Toolbox.extractFilename(path)
}