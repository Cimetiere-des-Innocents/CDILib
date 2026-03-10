package xyz.cimetieredesinnocents.cdilib.network

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.network.handling.IPayloadContext
import org.joml.Vector3f
import kotlin.reflect.KMutableProperty1

@Suppress("Unused")
abstract class LibBasePacket<P : Any>(
    modid: String,
    val name: String,
    val direction: Direction,
    val phase: Phase
) {
    enum class Direction {
        TO_CLIENT,
        TO_SERVER,
        BIDIRECTIONAL
    }

    enum class Phase {
        PLAY,
        CONFIGURATION,
        COMMON
    }

    protected abstract val factory: () -> P

    abstract val codec: StreamCodec<ByteBuf, Packet<P>>

    protected abstract class BaseDataBuilder<P : Any> {
        abstract fun build(): StreamCodec<ByteBuf, P>
    }

    protected class InitialDataBuilder<P : Any, D>(
        val baseCodec: StreamCodec<ByteBuf, D>,
        val factory: () -> P,
        val property: KMutableProperty1<P, D>
    ) : BaseDataBuilder<P>() {
        val codec = object : StreamCodec<ByteBuf, P> {
            override fun decode(buffer: ByteBuf): P {
                val value = baseCodec.decode(buffer)
                val returnValue = factory()
                property.set(returnValue, value)
                return returnValue
            }

            override fun encode(buffer: ByteBuf, value: P) {
                baseCodec.encode(buffer, property.get(value)!!)
            }
        }

        fun int(property: KMutableProperty1<P, Int>) =
            DataBuilder(ByteBufCodecs.INT, this, property)

        fun float(property: KMutableProperty1<P, Float>) =
            DataBuilder(ByteBufCodecs.FLOAT, this, property)

        fun double(property: KMutableProperty1<P, Double>) =
            DataBuilder(ByteBufCodecs.DOUBLE, this, property)

        fun boolean(property: KMutableProperty1<P, Boolean>) =
            DataBuilder(ByteBufCodecs.BOOL, this, property)

        fun string(property: KMutableProperty1<P, String>) =
            DataBuilder(ByteBufCodecs.STRING_UTF8, this, property)

        fun vector3f(property: KMutableProperty1<P, Vector3f>) =
            DataBuilder(ByteBufCodecs.VECTOR3F, this, property)

        override fun build() = codec
    }

    protected class DataBuilder<P : Any, T : BaseDataBuilder<P>, D>(
        val baseCodec: StreamCodec<ByteBuf, D>,
        val previousBuilder: T,
        val property: KMutableProperty1<P, D>
    ) : BaseDataBuilder<P>() {
        val codec = object : StreamCodec<ByteBuf, P> {
            val previousCodec = previousBuilder.build()
            override fun decode(buffer: ByteBuf): P {
                val returnValue = previousCodec.decode(buffer)
                val value = baseCodec.decode(buffer)
                property.set(returnValue, value)
                return returnValue
            }

            override fun encode(buffer: ByteBuf, value: P) {
                previousCodec.encode(buffer, value)
                baseCodec.encode(buffer, property.get(value)!!)
            }
        }

        fun int(property: KMutableProperty1<P, Int>) =
            DataBuilder(ByteBufCodecs.INT, this, property)

        fun float(property: KMutableProperty1<P, Float>) =
            DataBuilder(ByteBufCodecs.FLOAT, this, property)

        fun double(property: KMutableProperty1<P, Double>) =
            DataBuilder(ByteBufCodecs.DOUBLE, this, property)

        fun boolean(property: KMutableProperty1<P, Boolean>) =
            DataBuilder(ByteBufCodecs.BOOL, this, property)

        fun string(property: KMutableProperty1<P, String>) =
            DataBuilder(ByteBufCodecs.STRING_UTF8, this, property)

        fun vector3f(property: KMutableProperty1<P, Vector3f>) =
            DataBuilder(ByteBufCodecs.VECTOR3F, this, property)

        override fun build() = codec
    }

    val packetType = CustomPacketPayload.Type<Packet<P>>(ResourceLocation.fromNamespaceAndPath(modid, name))

    class Packet<P : Any>(
        private val base: LibBasePacket<P>,
        val data: P
    ) : CustomPacketPayload {
        override fun type() = base.packetType
    }

    protected fun int(property: KMutableProperty1<P, Int>) =
        InitialDataBuilder(ByteBufCodecs.INT, factory, property)

    protected fun float(property: KMutableProperty1<P, Float>) =
        InitialDataBuilder(ByteBufCodecs.FLOAT, factory, property)

    protected fun double(property: KMutableProperty1<P, Double>) =
        InitialDataBuilder(ByteBufCodecs.DOUBLE, factory, property)

    protected fun boolean(property: KMutableProperty1<P, Boolean>) =
        InitialDataBuilder(ByteBufCodecs.BOOL, factory, property)

    protected fun string(property: KMutableProperty1<P, String>) =
        InitialDataBuilder(ByteBufCodecs.STRING_UTF8, factory, property)

    protected fun vector3f(property: KMutableProperty1<P, Vector3f>) =
        InitialDataBuilder(ByteBufCodecs.VECTOR3F, factory, property)

    protected fun codec(builder: BaseDataBuilder<P>): StreamCodec<ByteBuf, Packet<P>> {
        val self = this
        return object : StreamCodec<ByteBuf, Packet<P>> {
            val handler = self
            val codec = builder.build()

            override fun decode(buffer: ByteBuf): Packet<P> {
                val obj = codec.decode(buffer)
                return Packet(handler, obj)
            }

            override fun encode(buffer: ByteBuf, value: Packet<P>) {
                codec.encode(buffer, value.data)
            }

        }
    }

    open fun onServerReceived(packet: P, context: IPayloadContext) {}

    open fun onClientReceived(packet: P, context: IPayloadContext) {}

    override fun equals(other: Any?): Boolean {
        return other is LibBasePacket<*> && name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    fun packet(putData: (dataObj: P) -> Unit): Packet<P> {
        val dataObj = factory()
        putData(dataObj)
        return Packet(this, dataObj)
    }
}