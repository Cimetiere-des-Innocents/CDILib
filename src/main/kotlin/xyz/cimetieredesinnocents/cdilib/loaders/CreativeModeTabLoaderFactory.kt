package xyz.cimetieredesinnocents.cdilib.loaders

import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

open class CreativeModeTabLoaderFactory(private val modid: String) {
    private val registry = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, modid)
    fun register(
        name: String,
        items: ItemLoaderFactory,
        icon: () -> ItemStack = { ItemStack.EMPTY }
    ): DeferredHolder<CreativeModeTab?, CreativeModeTab> {
        return registry.register(name) { ->
            CreativeModeTab.builder()
                .title(Component.translatable("tab.${modid}.${name}"))
                .icon(icon)
                .displayItems { parameters, output -> items.registerToTab(output) }
                .build()
        }
    }

    fun bootstrap(bus: IEventBus) {
        registry.register(bus)
    }
}