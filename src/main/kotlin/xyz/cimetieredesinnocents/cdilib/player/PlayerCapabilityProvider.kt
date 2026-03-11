package xyz.cimetieredesinnocents.cdilib.player

import net.minecraft.world.entity.player.Player
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.capabilities.EntityCapability
import net.neoforged.neoforge.capabilities.ICapabilityProvider
import net.neoforged.neoforge.event.entity.player.PlayerEvent

open class PlayerCapabilityProvider<I : PlayerCapabilityBase, T : I>(
    val interfaceClass: Class<I>,
    val builder: (player: Player) -> T,
    val type: () -> EntityCapability<I, Void?>
) : ICapabilityProvider<Player, Void?, I> {
    private val serverMap = hashMapOf<Player, T>()
    private val clientMap = hashMapOf<Player, T>()

    private fun clearMap(map: HashMap<Player, T>) {
        for (player in map.keys) {
            if (player.removalReason != null) {
                map.remove(player)
            }
        }
    }

    private fun clearMaps() {
        clearMap(serverMap)
        clearMap(clientMap)
    }

    private fun getCapabilityInMap(player: Player, map: HashMap<Player, T>): T {
        if (player !in map) {
            map[player] = builder(player)
        }

        return map[player]!!
    }

    @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE")
    override fun getCapability(player: Player, context: Void?): I {
        return if (player.level().isClientSide) {
            getCapabilityInMap(player, clientMap)
        } else {
            getCapabilityInMap(player, serverMap)
        }
    }

    fun bootstrap(bus: IEventBus) {
        bus.addListener(PlayerEvent.PlayerRespawnEvent::class.java) {
            val player = it.entity
            val cap = player.getCapability(type()) ?: return@addListener
            val oldPlayer = cap.player
            if (oldPlayer != player) {
                clearMaps()
            }
            cap.player = player
            cap.afterRespawn()
        }

        bus.addListener(PlayerEvent.PlayerLoggedInEvent::class.java) {
            clearMaps()
        }

        bus.addListener(PlayerEvent.PlayerLoggedOutEvent::class.java) {
            clearMaps()
        }
    }
}