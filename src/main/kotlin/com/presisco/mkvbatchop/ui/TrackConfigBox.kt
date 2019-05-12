package com.presisco.mkvbatchop.ui

import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import tornadofx.*

class TrackConfigBox(
        private val trackType: String
) : Fragment() {

    private lateinit var selectionModeCombo: ComboBox<String>
    private lateinit var matchModeCombo: ComboBox<String>
    private lateinit var patternText: TextField

    private lateinit var titlesCheckBox: CheckBox
    private lateinit var languagesCheckBox: CheckBox
    private lateinit var offsetCheckBox: CheckBox
    private lateinit var titlesText: TextField
    private lateinit var languagesText: TextField
    private lateinit var offsetText: TextField

    private lateinit var matchPatternBox: HBox

    override val root = vbox {
        hbox {
            add(label { text = "$trackType selection" })
            selectionModeCombo = combobox(values = arrayListOf(
                    "none",
                    "all",
                    "first",
                    "match"
            )) {
                value = "none"
            }
            matchPatternBox = hbox {
                label { text = "selection_mode" }
                matchModeCombo = combobox(values = arrayListOf(
                        "title",
                        "id",
                        "language"
                ))
                patternText = textfield { text = "" }
            }
        }
        hbox {
            label { text = "$trackType edit settings" }
            hbox {
                titlesCheckBox = checkbox("title")
                titlesText = textfield {}
            }
            hbox{
                languagesCheckBox = checkbox("language")
                languagesText = textfield {}
            }
            hbox{
                offsetCheckBox = checkbox("offset(ms)")
                offsetText = textfield {}
            }
        }
    }

    init {
        selectionModeCombo.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            if (newValue == "match") {
                matchPatternBox.isVisible=true
                matchPatternBox.isManaged=true
            } else {
                matchPatternBox.isVisible=false
                matchPatternBox.isManaged=false
            }
        }
    }

    fun readSelection(): HashMap<String, Any>{
        val matchPattern = patternText.text.trim().split(",")
        val selectionConf = hashMapOf<String, Any>()
        selectionConf["mode"] = selectionModeCombo.value
        if(selectionModeCombo.value == "match"){
            selectionConf[matchModeCombo.value] = matchPattern
        }
        return selectionConf
    }

    fun readEditSettings(): HashMap<String, Any>{
        val editConf = hashMapOf<String, Any>()
        if(titlesCheckBox.isSelected){
            editConf["title"] = titlesText.text.trim().split(",")
        }
        if(languagesCheckBox.isSelected){
            editConf["language"] = languagesText.text.trim().split(",")
        }
        if(offsetCheckBox.isSelected){
            editConf["offset"] = offsetText.text.trim().toInt()
        }
        return editConf
    }

    fun updateSelection(conf: Map<String, Any>){
        val selectionMode = conf["mode"] as String
        selectionModeCombo.value = selectionMode
        if(selectionMode == "match"){
            val matchMode = conf.keys.toList()[1]
            val matchPattern = conf[matchMode] as List<String>
            matchPatternBox.show()
            matchModeCombo.value = matchMode
            patternText.text = matchPattern.joinToString(",")
        }else{
            matchPatternBox.hide()
        }
    }

    fun updateEditSettings(conf: Map<String, Any>){
        fun clearTitles(){
            titlesCheckBox.isSelected = false
            titlesText.text = ""
        }

        fun clearLanguages(){
            languagesCheckBox.isSelected = false
            languagesText.text = ""
        }

        fun clearOffset(){
            offsetCheckBox.isSelected = false
            offsetText.text = ""
        }

        conf["title"]?.let {
            val titles = it as List<String>
            titlesCheckBox.isSelected = true
            titlesText.text = titles.joinToString(",")
        }?: clearTitles()

        conf["language"]?.let {
            val languages = it as List<String>
            languagesCheckBox.isSelected = true
            languagesText.text = languages.joinToString(",")

        }?: clearLanguages()

        conf["offset"]?.let {
            val offset = it as Number
            offsetCheckBox.isSelected = true
            offsetText.text = offset.toInt().toString()

        }?: clearOffset()
    }
}