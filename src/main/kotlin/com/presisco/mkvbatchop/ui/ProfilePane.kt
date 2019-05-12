package com.presisco.mkvbatchop.ui

import com.presisco.mkvbatchop.Preference
import com.presisco.mkvbatchop.model.Profile
import com.presisco.mkvbatchop.ui.component.SimpleDropdownBox
import javafx.collections.FXCollections
import javafx.scene.control.SelectionMode
import javafx.scene.control.TextField
import tornadofx.*

class ProfilePane(
        private val profileMap: MutableMap<String, Profile>,
        private val onSelectProfile: (name: String) -> Unit,
        private val onUpdateProfile: (name: String) -> Unit = {}
) : View() {
    private val loadOrders = FXCollections.observableArrayList<String>()
    private lateinit var currentProfileName: String
    private lateinit var currentProfile: Profile
    private var currentOrderIndex = -1
    private lateinit var orderNameField: TextField

    private val configBoxMap = hashMapOf(
            "video" to TrackConfigBox("video"),
            "audio" to TrackConfigBox("audio"),
            "subtitle" to TrackConfigBox("subtitle")
    )

    private val onSelectValue = fun(_: String, newName: String) {
        if(profileMap.containsKey(newName)) {
            loadOrders.clear()
            currentProfileName = newName
            currentProfile = profileMap[currentProfileName]!!
            currentProfile.orders.forEachIndexed { index, order -> loadOrders.add("${index + 1}:${order.name}") }
            onSelectOrder(0)
            onSelectProfile(currentProfileName)
        }
    }

    private val onAddProfile = fun(newName: String) {
        loadOrders.clear()
        profileMap[newName] = Preference.newProfile(newName)
        currentProfile = profileMap[newName]!!
        loadOrders.add("1:${currentProfile.orders[0].name}")
    }

    private val onDeleteProfile = fun(oldName: String, newName: String) {
        profileMap.remove(oldName)
        onSelectValue(oldName, newName)
    }

    override val root = vbox {
        add(SimpleDropdownBox("profile", profileMap.keys, onSelectValue, onAddProfile, onDeleteProfile).root)
        hbox {
            vbox {
                label { text = "load order" }
                listview(loadOrders) {
                    id = "load_order"
                    selectionModel.selectionMode = SelectionMode.SINGLE
                    selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                        newValue?.let{onSelectOrder(newValue.substringBefore(":").toInt() - 1)}
                    }
                    maxHeight = 200.0
                }
            }
            vbox {
                hbox {
                    label{ text = "name" }
                    orderNameField = textfield {  }
                    button {
                        text = "add"
                        action {
                            val newName = "directory"
                            loadOrders.add("${loadOrders.size + 1}:$newName")
                            currentProfile.orders.add(Preference.newProfileOrder(newName))
                        }
                    }
                    button {
                        text = "delete"
                        action {
                            loadOrders.removeAt(1)
                        }
                    }
                }
                vbox {
                    add(configBoxMap["video"]!!.root)
                    add(configBoxMap["audio"]!!.root)
                    add(configBoxMap["subtitle"]!!.root)
                }
                button {
                    text = "save"
                    action {
                        val currentOrder = currentProfile.orders[currentOrderIndex]

                        configBoxMap.forEach {
                            type, box ->
                            currentOrder.configs[type]!!.selection = box.readSelection()
                            currentOrder.configs[type]!!.edit= box.readEditSettings()
                        }

                        val name = orderNameField.text
                        currentOrder.name = name
                        loadOrders[currentOrderIndex] = "${currentOrderIndex + 1}:$name"

                        Preference.writeProfiles(profileMap)
                        Preference.saveConfig()

                        onUpdateProfile(currentProfileName)
                    }
                }
            }
        }
    }

    init {
        onSelectValue("", profileMap.keys.first())
    }

    private fun onSelectOrder(index: Int) {
        currentOrderIndex = index
        val currentOrder = currentProfile.orders[currentOrderIndex]
        orderNameField.text = currentOrder.name
        configBoxMap.forEach {
            type, box ->
            val typeConf = currentOrder.configs[type]!!
            box.updateSelection(typeConf.selection)
            box.updateEditSettings(typeConf.edit)
        }
    }
}