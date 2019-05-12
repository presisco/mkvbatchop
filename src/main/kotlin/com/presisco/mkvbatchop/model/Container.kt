package com.presisco.mkvbatchop.model

data class Container(
        val path: String,
        val type: String,
        val videos: List<Track>,
        val audios: List<Track>,
        val subtitles: List<Track>
){
    companion object {
        private fun List<Track>.select(selection: Map<String, *>): List<Track>{

            fun Map<String,*>.getStringList(key: String) = this[key] as List<String>

            return when(selection["mode"] as String){
                "none" -> listOf()
                "first" -> listOf(this[0])
                "all" -> this
                "match" -> {
                    if(selection.containsKey("title")){
                        this.filter { selection.getStringList("title").contains(it.title) }
                    }else if(selection.containsKey("language")){
                        this.filter { selection.getStringList("language").contains(it.language) }
                    }else if(selection.containsKey("id")){
                        this.filter { selection.getStringList("id").contains(it.id.toString()) }
                    }else {
                        listOf()
                    }
                }
                else -> listOf()
            }
        }

        private fun List<Track>.edit(edit: Map<String, *>): List<Track>{
            this.forEachIndexed{
                index, track ->
                fun getValue(values: List<String>): String{
                    return if(index < values.size ){
                        values[index]
                    }else{
                        values.last()
                    }
                }
                edit["title"]?.let {
                    track.title = getValue(it as List<String>)
                }
                edit["language"]?.let {
                    track.language = getValue(it as List<String>)
                }
                edit["offset"]?.let {
                    track.offset = (it as Number).toInt()
                }
            }
            return this
        }

        fun build(identity: Map<String, *>, order: Profile.Order): Container {

            fun Map<String, *>.getMap(key: String) = this[key] as Map<String, *>
            fun Map<String, *>.getList(key: String) = this[key] as List<*>
            fun Map<String, *>.getString(key: String) = if (containsKey(key)) this[key] as String else ""
            fun Map<String, *>.getBoolean(key: String) = if (containsKey(key)) this[key] as Boolean else true
            fun Map<String, *>.getInt(key: String) = (this[key] as Number).toInt()
            val containerIdentity = identity.getMap("container")
            val path = identity.getString("file_name")
            if (!containerIdentity.getBoolean("recognized") || !containerIdentity.getBoolean("supported")) {
                throw IllegalStateException("unsupported container@$path")
            }

            val tracksIdentity = identity.getList("tracks")
            val videos = ArrayList<Track>()
            val audios = ArrayList<Track>()
            val subtitles = ArrayList<Track>()

            tracksIdentity.forEach {
                val map = it as Map<String, *>
                val props = map.getMap("properties")
                    val track = Track(
                            map.getInt("id"),
                            props.getString("track_name"),
                            props.getString("language"),
                            map.getString("type"),
                            map.getString("codec")
                    )
                    when (track.type) {
                        "video" -> videos.add(track)
                        "audio" -> audios.add(track)
                        "subtitles" -> subtitles.add(track)
                        else -> { println("unsupported type: ${track.type}") }
                    }
            }

            fun opWrapper(tracks: List<Track>, type: String): List<Track> {
                val typeConfig = order.configs[type]!!
                return tracks.select(typeConfig.selection)
                        .edit(typeConfig.edit)
            }

            return Container(
                    path,
                    containerIdentity.getString("type"),
                    opWrapper(videos, "video"),
                    opWrapper(audios, "audio"),
                    opWrapper(subtitles, "subtitle")
            )
        }
    }
}