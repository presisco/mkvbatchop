package com.presisco.mkvbatchop.model

data class Track(
        val id: Int,
        var title: String,
        var language: String,
        val type: String,
        val codec: String,
        var offset: Int = 0
)