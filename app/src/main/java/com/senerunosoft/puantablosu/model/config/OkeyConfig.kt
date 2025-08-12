package com.senerunosoft.puantablosu.model.config

import com.senerunosoft.puantablosu.model.enums.RuleType

// Yeni obje bazlı yapı

data class OkeyConfig(
    var isPartnered: Boolean = true,
    val rules: List<RuleConfig> = listOf()
) : IConfig
