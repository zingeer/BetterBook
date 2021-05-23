package com.github.zingeer.book

import com.github.rqbik.bukkt.extensions.event.register
import com.github.zingeer.book.utils.TagManager
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

lateinit var plugin: Plugin

lateinit var lang: String
lateinit var localization: YamlConfiguration

class Plugin : JavaPlugin() {

    override fun onEnable() {
        plugin = this

        loadSetup()
        loadLang()

        TagManager.initialization(this)
        EventListener.register(this)

    }

    override fun onDisable() {

    }

    fun loadSetup() {
        val directory = File(dataFolder.absolutePath)
        val file = File(directory, "setup.yml")
        if (!file.exists()) {
            directory.mkdirs()
            file.createNewFile()
        }
        lang = YamlConfiguration.loadConfiguration(file).getString("lang") ?: run {
            config.set("lang", "en_EN")
            config.save(file)
            "en_EN"
        }
    }

    fun loadLang() {
        localization = YamlConfiguration.loadConfiguration(getTextResource("lang/$lang.yml") ?: return)
    }

}