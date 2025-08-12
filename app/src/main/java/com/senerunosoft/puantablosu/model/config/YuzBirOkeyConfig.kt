package com.senerunosoft.puantablosu.model.config

import com.senerunosoft.puantablosu.model.enums.RuleType

data class YuzBirOkeyConfig(
    var isPartnered: Boolean = true,
    val rules: List<RuleConfig> = listOf(
        RuleConfig(
            key = "penalty",
            label = "Ceza Puanı",
            types = setOf(RuleType.PlayerPenaltyScore),
            value = "101"
        ),
        RuleConfig(
            key = "normalFinish",
            label = "Normal Bitiş",
            types = setOf(RuleType.FinishScore),
            value = "-101",
            pairedKey = "noOpenPenalty"
        ),
        RuleConfig(
            key = "noOpenPenalty",
            label = "Açmama Cezası",
            types = setOf(RuleType.FinishScore),
            value = "202",
        ),
        RuleConfig(
            key = "handFinish",
            label = "Elden Bitme",
            types = setOf(RuleType.FinishScore),
            value = "-202",
            pairedKey = "handNoOpenPenalty"
        ),
        RuleConfig(
            key = "handNoOpenPenalty",
            label = "Elden Bitme Açmama Cezası",
            types = setOf(RuleType.FinishScore),
            value = "404",
        ),
        RuleConfig(
            key = "handOkeyFinish",
            label = "Okeyle Elden Bitme",
            types = setOf(RuleType.FinishScore),
            value = "-404",
            pairedKey = "handOkeyNoOpenPenalty"
        ),
        RuleConfig(
            key = "handOkeyNoOpenPenalty",
            label = "Okeyle Elden Bitme Açamama Cezası",
            types = setOf(RuleType.FinishScore),
            value = "808",
        )
    )
) : IConfig
