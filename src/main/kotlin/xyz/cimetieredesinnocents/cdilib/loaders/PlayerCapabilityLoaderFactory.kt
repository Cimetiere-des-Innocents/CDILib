package xyz.cimetieredesinnocents.cdilib.loaders

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.capabilities.EntityCapability
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import xyz.cimetieredesinnocents.cdilib.player.PlayerCapabilityBase
import xyz.cimetieredesinnocents.cdilib.player.PlayerCapabilityProvider

open class PlayerCapabilityLoaderFactory(private val modid: String) {
    private class RegistryItem<I : PlayerCapabilityBase>(
        val raw: PlayerCapabilityProvider<I, *>,
        val cap: EntityCapability<I, Void?>
    ) {
        fun register(event: RegisterCapabilitiesEvent) {
            event.registerEntity(cap, EntityType.PLAYER, raw)
        }
    }
    private val registry = arrayListOf<RegistryItem<*>>()

    fun <I : PlayerCapabilityBase, T : I> register(
        name: String,
        provider: PlayerCapabilityProvider<I, T>
    ): EntityCapability<I, Void?> {
        val cap = EntityCapability.createVoid(
            ResourceLocation.fromNamespaceAndPath(modid, name),
            provider.interfaceClass
        )
        registry.add(RegistryItem(provider, cap))
        return cap
    }

    fun bootstrap(modBus: IEventBus, forgeBus: IEventBus) {
        modBus.addListener(RegisterCapabilitiesEvent::class.java) {
            for (cap in registry) {
                cap.register(it)
            }
        }

        for (cap in registry) {
            cap.raw.bootstrap(forgeBus)
        }
    }
}