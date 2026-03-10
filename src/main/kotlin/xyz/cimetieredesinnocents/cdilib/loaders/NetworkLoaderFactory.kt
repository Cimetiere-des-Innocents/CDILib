package xyz.cimetieredesinnocents.cdilib.loaders

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler
import net.neoforged.neoforge.network.registration.PayloadRegistrar
import xyz.cimetieredesinnocents.cdilib.network.LibBasePacket

open class NetworkLoaderFactory {
    private val packets = arrayListOf<LibBasePacket<*>>()

    fun <T : LibBasePacket<*>> register(packet: T) : T {
        packets.add(packet)
        return  packet
    }

    fun <T : Any> register(registrar: PayloadRegistrar, packet: LibBasePacket<T>) {
        when (packet.phase) {
            LibBasePacket.Phase.PLAY -> {
                when (packet.direction) {
                    LibBasePacket.Direction.TO_CLIENT -> {
                        registrar.playToClient(packet.packetType, packet.codec) { payload, context ->
                            packet.onClientReceived(payload.data, context)
                        }
                    }
                    LibBasePacket.Direction.TO_SERVER -> {
                        registrar.playToServer(packet.packetType, packet.codec) { payload, context ->
                            packet.onServerReceived(payload.data, context)
                        }
                    }
                    else -> {
                        registrar.playBidirectional(packet.packetType, packet.codec, DirectionalPayloadHandler(
                            { payload, context -> packet.onClientReceived(payload.data, context) },
                            { payload, context -> packet.onServerReceived(payload.data, context) }
                        ))
                    }
                }
            }
            LibBasePacket.Phase.CONFIGURATION -> {
                when (packet.direction) {
                    LibBasePacket.Direction.TO_CLIENT -> {
                        registrar.configurationToClient(packet.packetType, packet.codec) { payload, context ->
                            packet.onClientReceived(payload.data, context)
                        }
                    }
                    LibBasePacket.Direction.TO_SERVER -> {
                        registrar.configurationToServer(packet.packetType, packet.codec) { payload, context ->
                            packet.onServerReceived(payload.data, context)
                        }
                    }
                    else -> {
                        registrar.configurationBidirectional(packet.packetType, packet.codec, DirectionalPayloadHandler(
                            { payload, context -> packet.onClientReceived(payload.data, context) },
                            { payload, context -> packet.onServerReceived(payload.data, context) }
                        ))
                    }
                }
            }
            else -> {
                when (packet.direction) {
                    LibBasePacket.Direction.TO_CLIENT -> {
                        registrar.commonToClient(packet.packetType, packet.codec) { payload, context ->
                            packet.onClientReceived(payload.data, context)
                        }
                    }
                    LibBasePacket.Direction.TO_SERVER -> {
                        registrar.commonToServer(packet.packetType, packet.codec) { payload, context ->
                            packet.onServerReceived(payload.data, context)
                        }
                    }
                    else -> {
                        registrar.commonBidirectional(packet.packetType, packet.codec, DirectionalPayloadHandler(
                            { payload, context -> packet.onClientReceived(payload.data, context) },
                            { payload, context -> packet.onServerReceived(payload.data, context) }
                        ))
                    }
                }
            }
        }
    }

    fun bootstrap(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar("1")
        for (packet in packets) {
            register(registrar, packet)
        }
    }
}