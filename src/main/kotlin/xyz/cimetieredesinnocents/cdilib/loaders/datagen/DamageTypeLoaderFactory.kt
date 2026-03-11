package xyz.cimetieredesinnocents.cdilib.loaders.datagen

import net.minecraft.core.Holder
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.damagesource.DamageEffects
import net.minecraft.world.damagesource.DamageScaling
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.damagesource.DeathMessageType
import net.minecraft.world.level.Level
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider
import xyz.cimetieredesinnocents.cdilib.loaders.DataGenLoaderFactory
import kotlin.reflect.KProperty

open class DamageTypeLoaderFactory(private val modid: String) {
    class RawDamageType(
        modid: String,
        val name: String,
        val scaling: DamageScaling,
        val exhaustion: Float,
        val effects: DamageEffects,
        val deathMessageType: DeathMessageType
    ) {
        val resourceKey = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(modid, name))
        operator fun getValue(thisRef: Any?, propertyKey: KProperty<*>): (Level) -> Holder.Reference<DamageType> {
            return { it.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(resourceKey) }
        }
    }

    val registry = hashSetOf<RawDamageType>()

    fun register(
        name: String,
        scaling: DamageScaling = DamageScaling.NEVER,
        exhaustion: Float = 0.1f,
        effects: DamageEffects = DamageEffects.HURT,
        deathMessageType: DeathMessageType = DeathMessageType.DEFAULT
    ): RawDamageType {
        val value = RawDamageType(modid, name, scaling, exhaustion, effects, deathMessageType)
        registry.add(value)
        return value
    }

    fun bootstrap(dataGen: DataGenLoaderFactory) {
        dataGen.register(DataGenLoaderFactory.Side.SERVER) {
            DatapackBuiltinEntriesProvider(
                it.output,
                it.lp,
                RegistrySetBuilder().add(Registries.DAMAGE_TYPE) { context ->
                    for (rawValue in registry) {
                        context.register(rawValue.resourceKey, DamageType(
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
        }
    }
}