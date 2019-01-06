package com.presisco.mkvbatchop

import org.junit.Test
import kotlin.test.expect

class ToolboxTest {

    @Test
    fun filenameExtractTest(){
        val path = "G:\\a dir\\sub_dir\\a file.txt"
        expect("a file.txt"){Toolbox.extractFilename(path)}
    }
}