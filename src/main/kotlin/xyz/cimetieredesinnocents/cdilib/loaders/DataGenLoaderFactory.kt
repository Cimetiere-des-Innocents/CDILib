package xyz.cimetieredesinnocents.cdilib.loaders

import net.minecraft.core.HolderLookup
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.data.ExistingFileHelper
import net.neoforged.neoforge.data.event.GatherDataEvent
import java.util.concurrent.CompletableFuture

typealias LP = CompletableFuture<HolderLookup.Provider>
typealias EFH = ExistingFileHelper

open class DataGenLoaderFactory {
    data class Context(
        val output: PackOutput,
        val lp: LP,
        val efh: EFH
    )

    private val serverRegistry = mutableListOf<(Context) -> DataProvider>()
    private val clientRegistry = mutableListOf<(Context) -> DataProvider>()

    fun server(factory: (Context) -> DataProvider) {
        serverRegistry.add(factory)
    }

    fun client(factory: (Context) -> DataProvider) {
        clientRegistry.add(factory)
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
        }
    }
}