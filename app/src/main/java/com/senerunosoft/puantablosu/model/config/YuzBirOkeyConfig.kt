package com.senerunosoft.puantablosu.model.config

import com.senerunosoft.puantablosu.model.enums.GameType

data class YuzBirOkeyConfig(
    var isPartnered: Boolean = true,
    var penalty: String = "101",
    var normalFinish: String = "-101",
    var noOpenPenalty: String = "202",
    var handFinish: String = "-202",
    var handNoOpenPenalty: String = "404",
    var handOkeyFinish: String = "-404",
    var handOkeyNoOpenPenalty: String = "808"
) : IConfig
