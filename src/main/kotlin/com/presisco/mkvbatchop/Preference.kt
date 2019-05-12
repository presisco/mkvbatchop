package com.presisco.mkvbatchop

import com.presisco.gsonhelper.ConfigMapHelper
import com.presisco.mkvbatchop.model.Profile

object Preference {
    const val CONFIG_FILE = "config.json"
    private var config: HashMap<String, Any>
    private val helper = ConfigMapHelper()

    init {
        config = try {
            helper.readConfigMap(CONFIG_FILE) as HashMap<String, Any>
        }catch (e: Exception){
            hashMapOf()
        }
        default("mkv_merge","mkvmerge")
        default("output_dir","./output")
        default("profile","default")
        default("profiles", hashMapOf(
                "default" to newProfile("default").toMap()
        ))
    }

    private fun default(key: String, value: Any){
        if(!config.containsKey(key)){
            config[key] = value
        }
    }

    fun read(key: String) = config[key]

    fun readProfiles() = (config["profiles"] as Map<String, *>).mapValues { Profile.fromMap(it.value as Map<String, Any>) }.toMutableMap()

    fun writeProfiles(profiles: Map<String, Profile>){
        config["profiles"] = profiles.mapValues { it.value.toMap() }
    }

    fun write(key: String, value: Any){
        config[key] = value
    }

    fun saveConfig(){
        helper.writeConfigMap(CONFIG_FILE, config)
    }

    fun newTypeProfile() = Profile.Order.TrackConfig(
            hashMapOf(
                    "mode" to "none"
            ),
            hashMapOf()
    )

    fun newProfileOrder(name: String) = Profile.Order(
            name,
            mapOf(
                    "video" to newTypeProfile(),
                    "audio" to newTypeProfile(),
                    "subtitle" to newTypeProfile()
            )
    )

    fun newProfile(name: String) = Profile(
            name,
            mutableListOf(newProfileOrder("directory"))
    )

}