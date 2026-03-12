package xyz.cimetieredesinnocents.cdilib.loaders

import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceKey
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import net.neoforged.neoforge.data.event.GatherDataEvent
import java.util.concurrent.CompletableFuture

typealias LP = CompletableFuture<HolderLookup.Provider>
typealias EFH = ExistingFileHelper

open class DataGenLoaderFactory(private val modid: String) {
    data class Context(
        val output: PackOutput,
        val lp: LP,
        val efh: EFH
    )
    
    class DataPackRegistryItem<T>(
        val registry: ResourceKey<out Registry<T>>,
        val provider: RegistrySetBuilder.RegistryBootstrap<T>
    ) {
        fun register(builder: RegistrySetBuilder) {
            builder.add(registry, provider)
        }
    }

    enum class Side {
        SERVER,
        CLIENT
    }

    private val serverRegistry = mutableListOf<(Context) -> DataProvider>()
    private val clientRegistry = mutableListOf<(Context) -> DataProvider>()

    fun server(factory: (Context) -> DataProvider) {
        serverRegistry.add(factory)
    }

    fun client(factory: (Context) -> DataProvider) {
        clientRegistry.add(factory)
    }

    fun register(side: Side, factory: (Context) -> DataProvider) {
        when (side) {
            Side.SERVER -> serverRegistry.add(factory)
            Side.CLIENT -> clientRegistry.add(factory)
        }
    }

    private val datapackRegistry = mutableListOf<DataPackRegistryItem<*>>()
    
    fun <T> datapack(registry: ResourceKey<out Registry<T>>, provider: RegistrySetBuilder.RegistryBootstrap<T>) {
        datapackRegistry.add(DataPackRegistryItem(registry, provider))
    }
    
    fun bootstrap(bus: IEventBus) {
        bus.addListener(GatherDataEvent::class.java) { event ->
            val lp = event.lookupProvider
            val efh = event.existingFileHelper
            for (factory in serverRegistry) {
                event.generator.addProvider(event.includeServer(), DataProvider.Factory {
                    factory(Context(it, lp, efh))
                })
            }
            for (factory in clientRegistry) {
                event.generator.addProvider(event.includeClient(), DataProvider.Factory {
                    factory(Context(it, lp, efh))
                })
            }
            event.generator.addProvider(event.includeServer(), DataProvider.Factory {
                val builder = RegistrySetBuilder()
                for (item in datapackRegistry) {
                    item.register(builder)
                }
                DatapackBuiltinEntriesProvider(it, lp, builder, setOf(modid))
            })
        }
    }
}