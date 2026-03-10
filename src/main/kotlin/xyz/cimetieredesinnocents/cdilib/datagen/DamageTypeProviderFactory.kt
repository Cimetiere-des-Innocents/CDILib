package xyz.cimetieredesinnocents.cdilib.datagen

import net.minecraft.core.HolderLookup
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.data.PackOutput
import net.minecraft.world.damagesource.DamageType
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider
import xyz.cimetieredesinnocents.cdilib.loaders.DamageTypeLoaderFactory
import java.util.concurrent.CompletableFuture

open class DamageTypeProviderFactory(
    modid: String,
    loader: DamageTypeLoaderFactory,
    output: PackOutput,
    registries: CompletableFuture<HolderLookup.Provider>
) : DatapackBuiltinEntriesProvider(
    output,
    registries,
    RegistrySetBuilder().add(Registries.DAMAGE_TYPE) {
        for (rawValue in loader.registry) {
            it.register(rawValue.resourceKey, DamageType(
                "${modid}.${rawValue.name}",
                rawValue.scaling,
                rawValue.exhaustion,
                rawValue.effects,
                rawValue.deathMessageType
            ))
        }
    },
    setOf(modid)
)