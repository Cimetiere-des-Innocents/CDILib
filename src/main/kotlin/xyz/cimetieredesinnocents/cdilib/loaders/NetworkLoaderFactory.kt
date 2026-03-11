package xyz.cimetieredesinnocents.cdilib.loaders

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler
import net.neoforged.neoforge.network.registration.PayloadRegistrar
import xyz.cimetieredesinnocents.cdilib.network.LibBasePacket

open class NetworkLoaderFactory {
    private val packets = arrayListOf<LibBasePacket<*, *>>()

    fun <T : LibBasePacket<*, *>> register(packet: T) : T {
        packets.add(packet)
        return  packet
    }

    fun <T : Any, B : FriendlyByteBuf> register(registrar: PayloadRegistrar, packet: LibBasePacket<T, B>) {
        when (packet.phase) {
            LibBasePacket.Phase.PLAY -> {
                @Suppress("UNCHECKED_CAST") val casted = packet as LibBasePacket<T, RegistryFriendlyByteBuf>
                when (casted.direction) {
                    LibBasePacket.Direction.TO_CLIENT -> {
                        registrar.playToClient(casted.packetType, casted.codec) { payload, context ->
                            casted.onClientReceived(payload.data, context)
                        }
                    }
                    LibBasePacket.Direction.TO_SERVER -> {
                        registrar.playToServer(casted.packetType, casted.codec) { payload, context ->
                            casted.onServerReceived(payload.data, context)
                        }
                    }
                    else -> {
                        registrar.playBidirectional(casted.packetType, casted.codec, DirectionalPayloadHandler(
                            { payload, context -> casted.onClientReceived(payload.data, context) },
                            { payload, context -> casted.onServerReceived(payload.data, context) }
                        ))
                    }
                }
            }
            LibBasePacket.Phase.CONFIGURATION -> {
                @Suppress("UNCHECKED_CAST") val casted = packet as LibBasePacket<T, FriendlyByteBuf>
                when (casted.direction) {
                    LibBasePacket.Direction.TO_CLIENT -> {
                        registrar.configurationToClient(casted.packetType, casted.codec) { payload, context ->
                            casted.onClientReceived(payload.data, context)
                        }
                    }
                    LibBasePacket.Direction.TO_SERVER -> {
                        registrar.configurationToServer(casted.packetType, casted.codec) { payload, context ->
                            casted.onServerReceived(payload.data, context)
                        }
                    }
                    else -> {
                        registrar.configurationBidirectional(casted.packetType, casted.codec, DirectionalPayloadHandler(
                            { payload, context -> casted.onClientReceived(payload.data, context) },
                            { payload, context -> casted.onServerReceived(payload.data, context) }
                        ))
                    }
                }
            }
            else -> {
                @Suppress("UNCHECKED_CAST") val casted = packet as LibBasePacket<T, FriendlyByteBuf>
                when (casted.direction) {
                    LibBasePacket.Direction.TO_CLIENT -> {
                        registrar.commonToClient(casted.packetType, casted.codec) { payload, context ->
                            casted.onClientReceived(payload.data, context)
                        }
                    }
                    LibBasePacket.Direction.TO_SERVER -> {
                        registrar.commonToServer(casted.packetType, casted.codec) { payload, context ->
                            casted.onServerReceived(payload.data, context)
                        }
                    }
                    else -> {
                        registrar.commonBidirectional(casted.packetType, casted.codec, DirectionalPayloadHandler(
                            { payload, context -> casted.onClientReceived(payload.data, context) },
                            { payload, context -> casted.onServerReceived(payload.data, context) }
                        ))
                    }
                }
            }
        }
    }

    fun bootstrap(bus: IEventBus) {
        bus.addListener(RegisterPayloadHandlersEvent::class.java) {
            val registrar = it.registrar("1")
            for (packet in packets) {
                register(registrar, packet)
            }
        }
    }
}