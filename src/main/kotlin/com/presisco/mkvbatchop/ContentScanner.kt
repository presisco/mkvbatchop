package com.presisco.mkvbatchop

import com.presisco.mkvbatchop.mkvtoolnix.MKVMergeHelper
import com.presisco.toolbox.concurrent.CollectionOperator
import java.io.File

class ContentScanner(
        mkvmergePath: String,
        private val depth: Int = 99
) {

    private val mkvmerge = MKVMergeHelper(mkvmergePath)

    fun scan(dir: String): List<Map<String, *>>{
        val fileTree = File(dir).walk()
        val pathList = mutableListOf<String>()
        fileTree.maxDepth(depth)
                .filter { it.isFile }
                .filter { supportedExtensions.contains(it.extension.toLowerCase()) }
                .forEach { pathList.add(it.absolutePath) }

        return scan(pathList)
    }

    //fun scan(pathList: List<String>): List<Map<String, *>> = CollectionOperator(threads, pathList, identify, update).execute()

    fun scan(pathList: List<String>): List<Map<String, *>> = pathList.map(identify)

    private val identify = fun (path: String) = mkvmerge.getIdentity(path)

    companion object {
        private val supportedExtensions = setOf("mkv","mka","mks","mp4","m4a","m4v","avi","srt","ssa","ass","aac","mp3","flac")
    }
}