package com.senerunosoft.puantablosu.model

import com.google.gson.annotations.SerializedName

data class Score(
    @SerializedName("scoreOrder")
    val scoreOrder: Int = 0,
    @SerializedName("scoreMap")
    var scoreMap: HashMap<String, Int> = HashMap()
) {
    // Empty constructor for Gson
    constructor() : this(0, HashMap())
}