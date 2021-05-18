package com.github.zingeer.book

import com.github.zingeer.book.localization
import com.github.zingeer.book.utils.TagManager.getTag
import com.github.zingeer.book.utils.TagManager.setTag
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

object BookManager {

    fun updateNumberPages(book: ItemStack, number: Int) {
        book.setTag("book.pages", number.toString())
        book.lore(listOf(
            Component.text( "${ChatColor.GRAY}${localization.get("number_of_pages")}: $number"),
            Component.text( " "),
            Component.text( "${ChatColor.GRAY}" + localization.get("prompt_one")),
            Component.text( "${ChatColor.GRAY}" + localization.get("prompt_two"))
        ))
    }

    fun updateNumberPages(book: ItemMeta, number: Int) {
        book.setTag("book.pages", number.toString())
        book.lore(listOf(
            Component.text( "${ChatColor.GRAY}${localization.get("number_of_pages")}: $number"),
            Component.text( " "),
            Component.text( "${ChatColor.GRAY}" + localization.get("prompt_one")),
            Component.text( "${ChatColor.GRAY}" + localization.get("prompt_two"))
        ))
    }

    fun ItemStack.getNumberPages(): Int? = getTag("book.pages")?.toInt()

    fun ItemMeta.getNumberPages(): Int? = getTag("book.pages")?.toInt()
}