package com.presisco.mkvbatchop

import com.presisco.mkvbatchop.mkvtoolnix.MKVMergeHelper
import org.junit.Test

class MKVMergeHelperTest {
    private val MKVMergeHelper = MKVMergeHelper("D:\\multimedia\\mkvtoolnix\\mkvmerge")

    @Test
    fun identityTest(){
        println(MKVMergeHelper.getIdentity("samples\\video.h264"))
        println(MKVMergeHelper.getIdentity("samples\\audio.aac"))
        println(MKVMergeHelper.getIdentity("samples\\chapters.xml"))
        println(MKVMergeHelper.getIdentity("samples\\sub.srt"))
    }
}