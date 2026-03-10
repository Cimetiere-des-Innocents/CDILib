package xyz.cimetieredesinnocents.cdilib.loaders

import net.minecraft.core.registries.Registries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

open class BlockLoaderFactory(
    modid: String,
    private val itemLoader: ItemLoaderFactory) {
    class BlockAndItsItem<T : Block>(
        blockHolder: DeferredHolder<Block, T>,
        itemHolder: DeferredHolder<Item, BlockItem>
    ) {
        val block by blockHolder
        val item by itemHolder
    }

    val blocks = DeferredRegister.create(Registries.BLOCK, modid)
    val blocksWithLoot = arrayListOf<DeferredHolder<Block, out Block>>()

    fun <T : Block> register(name: String, block: () -> T): BlockAndItsItem<T> {
        return register(name, 0, block)
    }

    fun <T : Block> register(name: String, priority: Int, block: () -> T): BlockAndItsItem<T> {
        val registeredBlock = blockOnly(name, block)
        blocksWithLoot.add(registeredBlock)
        val registeredItem = itemLoader.register(name, priority) { BlockItem(registeredBlock.get(), Item.Properties()) }
        return BlockAndItsItem(registeredBlock, registeredItem)
    }

    fun <T : Block> blockOnly(name: String, block: () -> T): DeferredHolder<Block, T> {
        val registeredBlock = blocks.register(name, block)
        blocksWithLoot.add(registeredBlock)
        return registeredBlock
    }

    fun bootstrap(bus: IEventBus) {
        blocks.register(bus)
    }
}