package com.senerunosoft.puantablosu.model

import java.util.UUID

data class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String = ""
) {
    // Empty constructor for Gson
    constructor() : this(UUID.randomUUID().toString(), "")
    
    // Constructor matching the original Java constructor
    constructor(name: String) : this(UUID.randomUUID().toString(), name)
}