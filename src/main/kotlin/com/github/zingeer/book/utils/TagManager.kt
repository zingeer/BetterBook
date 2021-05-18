package com.github.zingeer.book.utils

import com.github.rqbik.bukkt.extensions.get
import com.github.rqbik.bukkt.extensions.item.meta
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

object TagManager {

    private lateinit var PLUGIN: Plugin

    fun initialization(plugin: Plugin) {
        PLUGIN = plugin
    }

    fun ItemStack.setTag(key: String, value: String): ItemStack = meta {
        persistentDataContainer.set(NamespacedKey(PLUGIN, key), PersistentDataType.STRING, value)
    }

    fun ItemStack.addTag(key: String): ItemStack = meta {
        persistentDataContainer.set(NamespacedKey(PLUGIN, key), PersistentDataType.STRING, "true")
    }

    fun ItemStack.removeTag(key: String) = meta {
        persistentDataContainer.remove(NamespacedKey(PLUGIN, key))
    }

    fun ItemStack.getTag(key: String): String? =
        if (itemMeta != null) itemMeta.persistentDataContainer.get<String>(NamespacedKey(PLUGIN, key)) else null

    fun ItemStack.hasTag(key: String): Boolean = getTag(key) != null

    //!!Changes only the meta itself, not the item!!
    fun ItemMeta.setTag(key: String, value: String) = persistentDataContainer.set(NamespacedKey(PLUGIN, key), PersistentDataType.STRING, value)

    fun ItemMeta.addTag(key: String) = persistentDataContainer.set(NamespacedKey(PLUGIN, key), PersistentDataType.STRING, "true")


    fun ItemMeta.removeTag(key: String) = persistentDataContainer.remove(NamespacedKey(PLUGIN, key))


    fun ItemMeta.getTag(key: String): String? = persistentDataContainer.get<String>(NamespacedKey(PLUGIN, key))

    fun ItemMeta.hasTag(key: String): Boolean = getTag(key) != null


    fun Entity.setTag(key: String, value: String) =
        persistentDataContainer.set(NamespacedKey(PLUGIN, key), PersistentDataType.STRING, value)

    fun Entity.removeTag(key: String) = persistentDataContainer.remove(NamespacedKey(PLUGIN, key))

    fun Entity.getTag(key: String): String? =
        persistentDataContainer.get(NamespacedKey(PLUGIN, key), PersistentDataType.STRING)

    fun Entity.hasTag(key: String): Boolean = getTag(key) != null

    fun Entity.addTag(key: String) = setTag(key, "true")
}

