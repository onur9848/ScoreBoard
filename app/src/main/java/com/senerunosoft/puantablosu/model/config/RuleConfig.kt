package com.senerunosoft.puantablosu.model.config

import com.senerunosoft.puantablosu.model.enums.RuleType

/**
 * Her oyun kuralı için temel obje.
 * pairedKey: Eşleşmeli property için (örn: normalFinish <-> noOpenPenalty)
 */
data class RuleConfig(
    val key: String,
    val label: String,
    val types: Set<RuleType>,
    var value: String,
    val description: String? = null,
    val pairedKey: String? = null
)

