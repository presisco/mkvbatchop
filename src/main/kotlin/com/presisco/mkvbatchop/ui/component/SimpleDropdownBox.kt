package com.presisco.mkvbatchop.ui.component

import javafx.collections.FXCollections
import javafx.scene.Parent
import javafx.scene.control.ComboBox
import tornadofx.*

class SimpleDropdownBox(
        title: String,
        data: Collection<String>,
        onSelect: (oldValue: String, newValue: String) -> Unit,
        onAdd: (newValue: String) -> Unit,
        onDel: (oldValue: String, newValue: String) -> Unit
): Fragment() {
    private lateinit var comboBox: ComboBox<String>
    private val dropdownContent = FXCollections.observableArrayList<String>(data)

    override val root: Parent = hbox {
        label { text = title }
        comboBox = combobox(values = dropdownContent) {
            isEditable = true
            selectionModel.selectedItemProperty().addListener{
                _, oldValue, newValue ->
                oldValue?.let{onSelect(oldValue, newValue)}
            }
            value = data.first()
        }
        button {
            text = "add"
            action {
                if(!data.contains(comboBox.value)) {
                    dropdownContent.add(comboBox.value)
                    onAdd(comboBox.value)
                }
            }
        }
        button {
            text = "delete"
            action {
                dropdownContent.remove(comboBox.value)
                onDel(comboBox.value, dropdownContent.first())
                comboBox.value = dropdownContent.first()
            }
        }
    }
}