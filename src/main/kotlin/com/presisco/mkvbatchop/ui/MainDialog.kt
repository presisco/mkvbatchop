package com.presisco.mkvbatchop.ui

import com.presisco.mkvbatchop.*
import com.presisco.mkvbatchop.mkvtoolnix.MKVMergeHelper
import com.presisco.mkvbatchop.model.Container
import com.presisco.mkvbatchop.model.Task
import com.presisco.mkvbatchop.model.Track
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.scene.control.*
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import tornadofx.*

class MainDialog : View() {

    private val fileChooser = FileChooser()
    private val dirChooser = DirectoryChooser()
    private lateinit var taskListView: ListView<Task>
    private lateinit var fileListView: TableView<Container>
    private lateinit var trackTableView: TableView<Track>
    private lateinit var mkvmergeField: TextField
    private lateinit var outputDirField: TextField

    private lateinit var executeButton: Button
    private lateinit var scanButton: Button

    private val controller: MainDialogController by inject()

    override val root = borderpane {
        top = ProfilePane(controller.profileMap) { controller.selectProfile(it) }.root
        left = vbox {
            label { text = "task list" }
            taskListView = listview(controller.taskList) {
                selectionModel.selectionMode = SelectionMode.SINGLE
                selectionModel.selectedItemProperty().addListener {
                    _, _, newValue ->
                    newValue?.let{
                        controller.updateContainers4Task(it)
                    }
                }
                minHeight = 200.0
                maxHeight = 400.0
                maxWidth = 600.0
            }
            useMaxHeight = true
        }
        center = vbox {
            label { text = "container list" }
            fileListView = tableview(controller.fileList) {
                readonlyColumn("path", Container::path).cellFormat { text = Toolbox.extractFilename(it) }
                readonlyColumn("type", Container::type)
                selectionModel.selectionMode = SelectionMode.SINGLE
                selectionModel.selectedItemProperty().addListener {
                    _, _, newValue ->
                    newValue?.let{
                        controller.loadTracks(it)
                    }
                }
                minHeight = 100.0
                maxHeight = 200.0
                maxWidth = 600.0
            }
            label { text = "track list" }
            trackTableView = tableview(controller.trackList) {
                readonlyColumn("id", Track::id)
                readonlyColumn("type", Track::type)
                readonlyColumn("codec", Track::codec)
                readonlyColumn("title", Track::title)
                readonlyColumn("language", Track::language)
                readonlyColumn("offset", Track::offset)
                minHeight = 100.0
                maxHeight = 200.0
                maxWidth = 600.0
            }

        }
        bottom = hbox {
            scanButton = button {
                id = "add_directory"
                text = "add directory"
                action {
                    val fileList = fileChooser.showOpenMultipleDialog(currentWindow)
                    if (fileList != null) {
                        val pathList = fileList.map { it.path }

                        scanButton.isDisable = true
                        runAsync {
                            if (controller.taskList.isEmpty()) {
                                controller.createTask(pathList)
                            } else {
                                controller.addFilesToTask(pathList)
                            }
                        } ui {
                            scanButton.isDisable = false
                        }
                    }
                }
            }
            button {
                id = "choose_output_dir"
                text = "choose output dir"
                action {
                    val dir = dirChooser.showDialog(currentWindow)
                    controller.updateOutputDir(dir.path)
                    outputDirField.text = dir.path
                }
            }
            button {
                id = "clear_tasks"
                text = "clear tasks"
                action {
                    controller.clearAll()
                }
            }
            outputDirField = textfield {
                id = "output_dir"
                text = Preference.read("output_dir") as String
            }
            button {
                id = "choose_mkvmerge"
                text = "choose mkvmerge"
                action {
                    val file = fileChooser.showOpenDialog(currentWindow)
                    if (file != null) {
                        controller.updateMkvMerge(file.path)
                        mkvmergeField.text = file.path
                    }
                }
            }
            mkvmergeField = textfield {
                id = "mkvmerge_path"
                text = Preference.read("mkv_merge") as String
            }
            executeButton = button {
                id = "execute"
                text = "execute"
                action {
                    executeButton.isDisable = true
                    runAsync {
                        controller.merge()
                    } ui {
                        executeButton.isDisable = false
                    }
                }
            }
        }
        minWidth = 1200.0
        minHeight = 800.0
    }

    init {
        dirChooser.title = "choose directory"
    }
}

class MainDialogController : Controller() {
    val profileMap = Preference.readProfiles()
    private var currentProfile = profileMap[Preference.read("profile")]!!
    private lateinit var currentTask: Task

    private val taskWarehouse = TaskWarehouse()
    private var contentScanner = ContentScanner(Preference.read("mkv_merge") as String, 1)
    private var mergeExecutor = MergeExecutor(Preference.read("mkv_merge") as String)

    val taskList = FXCollections.observableArrayList<Task>()
    val fileList = FXCollections.observableArrayList<Container>()
    val trackList = FXCollections.observableArrayList<Track>()

    private var orderCounter = 0

    fun updateMkvMerge(path: String) {
        Preference.write("mkv_merge", path)
        contentScanner = ContentScanner(Preference.read("mkv_merge") as String, 1)
        mergeExecutor = MergeExecutor(Preference.read("mkv_merge") as String)
        Preference.saveConfig()
    }

    fun updateOutputDir(path: String) {
        Preference.write("output_dir", path)
        Preference.saveConfig()
        taskWarehouse.adjustOutputDir(path)
    }

    fun createTask(pathList: List<String>) {
        val identities = contentScanner.scan(pathList)
        taskWarehouse.createTasks(identities, currentProfile.orders[orderCounter])
        taskWarehouse.adjustOutputDir(Preference.read("output_dir") as String)
        taskList.addAll(taskWarehouse.tasks)
        if(orderCounter < currentProfile.orders.size - 1) {
            orderCounter++
        }
        currentTask = taskList.first()
    }

    fun addFilesToTask(pathList: List<String>) {
        val identities = contentScanner.scan(pathList)
        taskWarehouse.addContainersToTasks(identities, currentProfile.orders[orderCounter])
        if(orderCounter < currentProfile.orders.size - 1) {
            orderCounter++
        }
        fileList.clear()
        fileList.addAll(currentTask.containers)
    }

    fun clearAll(){
        taskWarehouse.clearAll()
        taskList.clear()
        fileList.clear()
        trackList.clear()
        orderCounter = 0
    }

    fun updateContainers4Task(task: Task) {
        currentTask = task
        fileList.clear()
        fileList.addAll(task.containers)
    }

    fun loadTracks(container: Container) {
        trackList.clear()
        trackList.addAll(container.videos)
        trackList.addAll(container.audios)
        trackList.addAll(container.subtitles)
    }

    fun selectProfile(name: String) {
        currentProfile = profileMap[name]!!
        taskWarehouse.reloadWithProfile(currentProfile)
        taskWarehouse.adjustOutputDir(Preference.read("output_dir") as String)
        taskList.clear()
        fileList.clear()
        trackList.clear()
        taskList.addAll(taskWarehouse.tasks)
    }

    fun merge(){
        mergeExecutor.execute(taskWarehouse.tasks, currentProfile)
    }
}