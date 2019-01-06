package com.presisco.mkvbatchop.model

data class Profile(
        val name: String,
        var chapters: Int,
        val orders: MutableList<Order>
) {

    fun toMap() = mapOf(
            "name" to name,
            "chapters" to chapters,
            "orders" to orders.map { it.toMap() }
    )

    data class Order(
            var name: String,
            val configs: Map<String, TrackConfig>
    ){
        fun toMap() = mapOf(
                "name" to name,
                "configs" to configs.mapValues { it.value.toMap() }
        )
        data class TrackConfig(
                var selection: MutableMap<String, Any>,
                var edit: MutableMap<String, Any>
        ){
            fun toMap() = mapOf(
                    "selection" to selection,
                    "edit" to edit
            )

            companion object {
                fun fromMap(map: Map<String, Any>) = TrackConfig(
                        (map["selection"] as Map<String, Any>).toMutableMap(),
                        (map["edit"] as Map<String, Any>).toMutableMap()
                )
            }
        }

        companion object {
            fun fromMap(map: Map<String, Any>) = Order(
                    map["name"] as String,
                    (map["configs"] as Map<String, Any>).mapValues { TrackConfig.fromMap(it.value as Map<String, Any>) }
            )
        }
    }

    companion object {

        fun fromMap(map: Map<String, Any>) = Profile(
                map.getOrDefault("name", "default") as String,
                (map["chapters"] as Number).toInt(),
                (map["orders"] as List<Map<String, Any>>).map { Order.fromMap(it) }.toMutableList()
        )
    }
}