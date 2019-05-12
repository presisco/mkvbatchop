package com.presisco.mkvbatchop

import com.presisco.mkvbatchop.mkvtoolnix.MKVMergeHelper
import com.presisco.mkvbatchop.model.Profile
import com.presisco.mkvbatchop.model.Task

class MergeExecutor(
        mkvmergePath: String
) {

    private val mkvmerge = MKVMergeHelper(mkvmergePath)

    /*
    fun execute(outputs: List<Task>, update: (Int, Task) -> Unit){
        CollectionOperator<Task, Unit>(threads, outputs, { mkvmerge.merge(it) }, update)
    }
    */

    fun execute(outputs: List<Task>, profile: Profile) = outputs.map { mkvmerge.merge(it, profile) }

}