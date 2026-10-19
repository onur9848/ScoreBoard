package com.senerunosoft.puantablosu

import android.content.Context
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        // Eski Kotlin sürümünün kayıtlarını Flutter tarafına aktarır (bir kez okunur).
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "puan_tablosu/legacy")
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "readGames" -> result.success(readLegacyGames())
                    "readRuleValues" -> result.success(readLegacyRuleValues())
                    else -> result.notImplemented()
                }
            }
    }

    private fun readLegacyGames(): List<String> {
        val prefs = getSharedPreferences("game", Context.MODE_PRIVATE)
        val ids = prefs.getString("gameIds", "").orEmpty()
        return ids.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { prefs.getString(it, null) }
    }

    private fun readLegacyRuleValues(): Map<String, String> {
        val prefs = getSharedPreferences("scoreboard_rules", Context.MODE_PRIVATE)
        return prefs.all.mapNotNull { (key, value) -> (value as? String)?.let { key to it } }.toMap()
    }
}
