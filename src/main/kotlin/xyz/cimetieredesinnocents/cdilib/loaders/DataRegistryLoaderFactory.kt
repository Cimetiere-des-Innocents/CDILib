package xyz.cimetieredesinnocents.cdilib.loaders

import com.mojang.serialization.Codec
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DataPackRegistryEvent

open class DataRegistryLoaderFactory(private val modid: String) {
    private class RegistryItem<T>(
        val key: ResourceKey<Registry<T>>,
        val codec: Codec<T>,
        val netCodec: Codec<T>
    ) {
        fun register(event: DataPackRegistryEvent.NewRegistry) {
            event.dataPackRegistry(key, codec, netCodec)
        }
    }

    private val registry = mutableListOf<RegistryItem<*>>()

    fun <T> register(name: String, codec: Codec<T>, netCodec: Codec<T>): ResourceKey<Registry<T>> {
        val key = ResourceKey.createRegistryKey<T>(
            ResourceLocation.fromNamespaceAndPath(modid, name)
        )
        registry.add(RegistryItem(key, codec, netCodec))
        return key
    }

    fun <T> register(name: String, codec: Codec<T>): ResourceKey<Registry<T>> {
        return register(name, codec, codec)
    }

    fun bootstrap(bus: IEventBus) {
        bus.addListener(DataPackRegistryEvent.NewRegistry::class.java) {
            for (item in registry) {
                item.register(it)
            }
        }
    }
}