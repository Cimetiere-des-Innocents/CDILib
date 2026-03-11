package xyz.cimetieredesinnocents.cdilib.loaders

import com.mojang.serialization.Codec
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

open class DataComponentLoaderFactory(modid: String) {
    val registry = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, modid)

    fun <T> register(
        name: String,
        codec: Codec<T?>,
        streamCodec: StreamCodec<in RegistryFriendlyByteBuf, T?>
    ): DeferredHolder<DataComponentType<*>, DataComponentType<T>> {
        return registry.registerComponentType(name) { it.persistent(codec).networkSynchronized(streamCodec) }
    }

    fun bootstrap(bus: IEventBus) {
        registry.register(bus)
    }
}