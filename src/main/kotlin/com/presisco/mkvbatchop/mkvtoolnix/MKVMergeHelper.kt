package com.presisco.mkvbatchop.mkvtoolnix

import com.presisco.gsonhelper.MapHelper
import com.presisco.mkvbatchop.model.Container
import com.presisco.mkvbatchop.model.Profile
import com.presisco.mkvbatchop.model.Task
import com.presisco.mkvbatchop.model.Track

class MKVMergeHelper(
        private val mkvMergePath: String
) {
    private val mapHelper = MapHelper()

    fun getIdentity(path: String): Map<String, *>{
        val proc = ProcessBuilder("\"$mkvMergePath\" -i -F json --ui-language en \"$path\"")
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

        val jsonText = proc.inputStream.bufferedReader().readText()
        return mapHelper.fromJson(jsonText)
    }

    fun buildCmd(output: String, inputContainers: List<Container>, profile: Profile): String{
        val args = mutableListOf<String>()

        fun buildTrackSelection(tracks: List<Track>, type: String){
            if(tracks.isEmpty()){
                args.add("--no-$type".replace("subtitle", "subtitles"))
            }else{
                args.add("--$type-tracks")
                val ids = tracks.map { it.id }
                args.add(ids.joinToString(","))
                args.addAll(tracks.map { "--language ${it.id}:${it.language}" })
                args.addAll(tracks.filter { it.title.isNotEmpty() }.map { "--track-name ${it.id}:\"${it.title}\"" })
                args.addAll(tracks.filter { it.offset != 0 }.map { "--sync ${it.id}:${it.offset}" })
            }
        }

        inputContainers.forEach {
            input ->
            buildTrackSelection(input.videos, "video")
            buildTrackSelection(input.audios, "audio")
            buildTrackSelection(input.subtitles, "subtitle")
            args.add("\"${input.path}\"")
        }

        //args.add("--chapters \"${inputContainers[profile.chapters].path}\"")
        val cmdBuilder = StringBuilder("\"$mkvMergePath\" -o \"$output\" ")
        cmdBuilder.append(args.joinToString(" "))
        return cmdBuilder.toString()
    }

    fun merge(task: Task, profile: Profile): String{
        val proc = ProcessBuilder(buildCmd(task.path, task.containers, profile))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

        val log = proc.inputStream.bufferedReader().readText()
        return log
    }
}