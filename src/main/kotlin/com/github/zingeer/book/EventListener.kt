package com.github.zingeer.book

import com.github.rqbik.bukkt.extensions.entity.give
import com.github.rqbik.bukkt.extensions.event.player
import com.github.rqbik.bukkt.extensions.item.displayName
import com.github.zingeer.book.localization
import com.github.rqbik.bukkt.extensions.scheduler
import com.github.zingeer.book.BookManager.getNumberPages
import com.github.zingeer.book.utils.TagManager.getTag
import com.github.zingeer.book.utils.TagManager.setTag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.*
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import java.time.LocalDate


object EventListener : Listener {

    @EventHandler
    fun onClickEvent(event: InventoryClickEvent) {
        val book = event.currentItem ?: return

        if (book.type == Material.WRITABLE_BOOK) {
            val pages = book.getNumberPages() ?: run {
                BookManager.updateNumberPages(book, 3)
                return@run 3
            }
            val paper = event.player.itemOnCursor
            if (event.click == ClickType.RIGHT && paper.type == Material.PAPER) {
                event.isCancelled = true
                BookManager.updateNumberPages(book, pages + paper.amount)
                val lines = paper.getTag("book.lines")?.toInt() ?: 0

                val page = Component.text().apply { component ->
                    (paper.lore() ?: listOf()).subList(0, lines).forEachIndexed { index, line ->
                        if (index != 0) component.append(Component.text("\n"))

                        component.append(line.color(null))
                    }
                }

                book.apply {
                    itemMeta = itemMeta.apply {
                        this as BookMeta
                        repeat(paper.amount) {
                            addPages(page.build())
                        }
                    }
                }

                paper.amount = 0
            }
        }
    }

    @EventHandler
    fun onPickUpEvent(event: EntityPickupItemEvent) {
        val item = event.item.itemStack

        if (item.type == Material.WRITABLE_BOOK) {
            item.getNumberPages() ?: run {
                BookManager.updateNumberPages(item, 3)
                return@run 3
            }
        }
    }

    @EventHandler
    fun onDragEvent(event: InventoryDragEvent) {
        mutableListOf<ItemStack?>().apply {
            addAll(event.newItems.map { it.value })
            add(event.oldCursor)
            add(event.cursor)
        }.forEach {
            val book = it ?: return
            if (book.type == Material.WRITABLE_BOOK) {
                book.getNumberPages() ?: run {
                    BookManager.updateNumberPages(book, 3)
                    return@run 3
                }
            }
        }
    }

    @EventHandler
    fun creativeClickEvent(event: InventoryCreativeEvent) {
        event.cursor.apply {
            if (type == Material.WRITABLE_BOOK) {
                getNumberPages() ?: run {
                    BookManager.updateNumberPages(this, 3)
                    return@run 3
                }
            }
        }
    }

    @EventHandler
    fun editBookEvent(event: PlayerEditBookEvent) {
        val bookMeta = event.newBookMeta

        val pages = bookMeta.getNumberPages() ?: run {
            BookManager.updateNumberPages(bookMeta, 3)
            return@run 3
        }
        if (bookMeta.pageCount > pages) {
            event.player.sendMessage("${ChatColor.RED}" + localization.get("more_than_pages"))
            event.newBookMeta = bookMeta.apply {
                pages(bookMeta.pages().subList(0, pages))
            }
            scheduler.runTaskLater(plugin, Runnable {
                event.player.updateInventory()
            }, 1)
        }
        if (event.isSigning) {
            val date = LocalDate.now()
            event.newBookMeta = bookMeta.apply {
                lore(listOf(Component.text("${date.dayOfMonth}-${date.monthValue}-${date.year}").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)))
            }
        }
    }

    @EventHandler
    fun onInteractEvent(event: PlayerInteractEvent) {
        val player = event.player

        if (player.isSneaking && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            val book = player.inventory.itemInMainHand
            if (book.type == Material.WRITABLE_BOOK) {
                event.isCancelled = true

                val bookMeta = ItemStack(Material.WRITTEN_BOOK).itemMeta as BookMeta

                (book.itemMeta as BookMeta).pages().forEachIndexed { index, page ->
                    bookMeta.addPages(
                        Component.text("        [${ChatColor.BLUE}X${ChatColor.BLACK}]")
                            .hoverEvent(HoverEvent.showText(Component.text(localization.get("the_signature").toString())))
                            .clickEvent(ClickEvent.runCommand("/paper $index true"))
                        .append(
                                Component.text("     ")
                        ).append(
                                Component.text("[${ChatColor.RED}X${ChatColor.BLACK}]         ")
                                    .hoverEvent(HoverEvent.showText(Component.text(localization.get("without_signature").toString())))
                                    .clickEvent(ClickEvent.runCommand("/paper $index false"))
                        ).append(
                                page.hoverEvent(HoverEvent.showText(Component.text())).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, ""))
                            )
                    )
                }
                player.openBook(bookMeta)
            }
        }
    }

    @EventHandler
    fun onCommandEvent(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        val command = event.message

        if (command.startsWith("/paper ")) {
            event.isCancelled = true
            val book = player.inventory.itemInMainHand
            if (book.type != Material.WRITABLE_BOOK) return
            val bookMeta = book.itemMeta as BookMeta

            val information = command.removePrefix("/paper ").split(" ")
            val page = bookMeta.page(information[0].toInt() + 1)
            val signature = information[1].toBoolean()

            val list = ItemStack(Material.PAPER).apply {
                displayName = "${ChatColor.WHITE}" + localization.get("list_name")

                lore((page as TextComponent).content()
                    .lines().map { line -> Component.text(line).color(NamedTextColor.GRAY) }.apply {
                        setTag("book.lines", size.toString())
                        plusElement(Component.text(" "))
                    })
                }

            if (signature) {
                val date = LocalDate.now()
                list.apply {
                    lore((lore() ?: listOf())
                        .plusElement(Component.text(localization.get("signature").toString() + ": " + player.name).color(NamedTextColor.GRAY))
                        .plusElement(Component.text("${date.dayOfMonth}-${date.monthValue}-${date.year}").color(NamedTextColor.GRAY))
                    )
                }
            }
            val newPages = (book.getNumberPages() ?: 0) - 1
            if (newPages > 0) {
                book.itemMeta = bookMeta.apply {
                    pages(mutableListOf<Component?>().apply {
                        addAll(bookMeta.pages())
                        removeAt(information[0].toInt())
                    })
                }
                BookManager.updateNumberPages(book, newPages)
            } else {
                book.amount -= 1
            }
            list.apply {
                val lore = mutableListOf<Component>()
                lore()?.forEach {
                    lore.add(it.decoration(TextDecoration.ITALIC, false))
                }
                lore(lore)
            }

            player.give(list)

            scheduler.runTaskLater(plugin, Runnable {
                event.player.updateInventory()
            }, 1)
        }
    }
}

