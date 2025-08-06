package com.senerunosoft.puantablosu.model

data class Score(
    val scoreOrder: Int = 0,
    var scoreMap: HashMap<String, Int> = HashMap()
) {
    // Empty constructor for Gson
    constructor() : this(0, HashMap())
    
    // Constructor matching the original Java constructor
    constructor(scoreOrder: Int, scoreMap: HashMap<String, Int>) : this() {
        this.scoreOrder = scoreOrder
        this.scoreMap = scoreMap
    }
    
    // Getter methods to match original Java API (for backward compatibility)
    fun getScoreOrder(): Int = scoreOrder
    
    fun getScoreMap(): HashMap<String, Int> = scoreMap
    
    fun setScoreMap(scoreMap: HashMap<String, Int>) {
        this.scoreMap = scoreMap
    }
}