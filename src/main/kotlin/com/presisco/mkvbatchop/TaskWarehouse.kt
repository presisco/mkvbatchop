package com.presisco.mkvbatchop

import com.presisco.mkvbatchop.model.Container
import com.presisco.mkvbatchop.model.Profile
import com.presisco.mkvbatchop.model.Task

class TaskWarehouse {
    val identityMatrix = ArrayList<ArrayList<Map<String, *>>>()
    val tasks = ArrayList<Task>()

    fun createTasks(identities: List<Map<String, *>>, order: Profile.Order, addToMatrix: Boolean = true){
        tasks.addAll(
                identities.map {
                    val container = Container.build(it, order)
                    Task(container.path, arrayListOf(container))
        })
        if(addToMatrix) {
            identityMatrix.add(ArrayList(identities))
        }
    }

    fun addContainersToTasks(identities: List<Map<String, *>>, order: Profile.Order, addToMatrix: Boolean = true){
        val count = if (identities.size > tasks.size) tasks.size else identities.size
        for (i in 0.until(count)){
            tasks[i].containers.add(
                    Container.build(identities[i], order)
            )
        }
        if(addToMatrix) {
            identityMatrix.add(ArrayList(identities))
        }
    }

    fun adjustOutputDir(dir: String) = tasks.forEach { it.path = "$dir\\${Toolbox.extractFilename(it.path)}" }

    fun clearAll(){
        tasks.clear()
        identityMatrix.clear()
    }

    fun reloadWithProfile(profile: Profile){
        tasks.clear()
        if(identityMatrix.size < 1)
            return

        val orders = profile.orders
        createTasks(identityMatrix[0], orders[0], false)
        val size = if(identityMatrix.size > orders.size) orders.size else identityMatrix.size
        for(i in 1.until(size)){
            addContainersToTasks(identityMatrix[i], orders[i], false)
        }
        if(identityMatrix.size > orders.size){
            for (i in orders.size.until(identityMatrix.size)){
                addContainersToTasks(identityMatrix[i], orders[orders.size - 1], false)
            }
        }
    }
}