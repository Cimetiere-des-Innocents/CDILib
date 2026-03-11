package xyz.cimetieredesinnocents.cdilib.datagen

import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.world.damagesource.DamageType
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider
import xyz.cimetieredesinnocents.cdilib.loaders.datagen.DamageTypeLoaderFactory
import xyz.cimetieredesinnocents.cdilib.loaders.DataGenLoaderFactory

open class DamageTypeProviderFactory(
    modid: String,
    loader: DamageTypeLoaderFactory,
    context: DataGenLoaderFactory.Context
) : DatapackBuiltinEntriesProvider(
    context.output,
    context.lp,
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