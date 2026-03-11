package xyz.cimetieredesinnocents.cdilib.loaders

import net.minecraft.core.registries.Registries
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.*

open class ItemLoaderFactory(modid: String) {
    class ItemInTab (
        val priority: Int,
        val index: Int,
        val item: DeferredHolder<Item, out Item>
    )

    private val items = DeferredRegister.create(Registries.ITEM, modid)

    private val itemsQueue = PriorityQueue(compareByDescending<ItemInTab>{it.priority}.thenBy{ it.index })

    private var globalIndex = 0

    fun <T : Item> register(name: String, item: () -> T): DeferredHolder<Item, T> {
        return register(name, 0, item)
    }

    fun <T : Item> register(name: String, priority: Int, item: () -> T): DeferredHolder<Item, T> {
        val registeredItem = items.register(name, item)
        itemsQueue.add(ItemInTab(priority, globalIndex, registeredItem))
        globalIndex++
        return registeredItem
    }

    fun simpleItem(name: String, priority: Int = 0) = register(name, priority) { Item(Item.Properties()) }

    fun <T : Item> registerHidden(name: String, item: () -> T): DeferredHolder<Item, T> {
        return items.register(name, item)
    }

    fun simpleItemHidden(name: String) = registerHidden(name) { Item(Item.Properties()) }

    fun registerToTab(output: CreativeModeTab.Output) {
        for (item in itemsQueue) {
            output.accept(item.item.get())
        }
    }

    fun bootstrap(bus: IEventBus) {
        items.register(bus)
    }
}