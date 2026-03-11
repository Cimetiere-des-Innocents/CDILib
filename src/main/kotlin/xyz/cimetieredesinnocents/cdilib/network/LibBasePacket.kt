package xyz.cimetieredesinnocents.cdilib.network

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.network.handling.IPayloadContext
import org.joml.Vector3f
import kotlin.reflect.KMutableProperty1

@Suppress("Unused")
abstract class LibBasePacket<P : Any, B : FriendlyByteBuf>(
    modid: String,
    val name: String,
    val direction: Direction,
    val phase: Phase<B>
) {
    enum class Direction {
        TO_CLIENT,
        TO_SERVER,
        BIDIRECTIONAL
    }

    private enum class RawPhase {
        PLAY,
        CONFIGURATION,
        COMMON
    }

    class Phase<T : FriendlyByteBuf> {
        private val value: RawPhase
        private constructor(value: RawPhase) {
            this.value = value
        }

        override fun equals(other: Any?): Boolean {
            if (other !is Phase<*>) {
                return false
            }

            return value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        companion object {
            val PLAY = Phase<RegistryFriendlyByteBuf>(RawPhase.PLAY)
            val CONFIGURATION = Phase<FriendlyByteBuf>(RawPhase.CONFIGURATION)
            val COMMON = Phase<FriendlyByteBuf>(RawPhase.COMMON)
        }
    }

    protected abstract val factory: () -> P

    abstract val codec: StreamCodec<B, Packet<P, B>>

    protected abstract class BaseDataBuilder<P : Any, B : FriendlyByteBuf> {
        abstract fun build(): StreamCodec<B, P>
    }

    protected class InitialDataBuilder<P : Any, B : FriendlyByteBuf, D>(
        val baseCodec: StreamCodec<in B, D>,
        val factory: () -> P,
        val property: KMutableProperty1<P, D>
    ) : BaseDataBuilder<P, B>() {
        val codec = object : StreamCodec<B, P> {
            override fun decode(buffer: B): P {
                val value = baseCodec.decode(buffer)
                val returnValue = factory()
                property.set(returnValue, value)
                return returnValue
            }

            override fun encode(buffer: B, value: P) {
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

        fun <T> custom(codec: StreamCodec<in B, T>, property: KMutableProperty1<P, T>) =
            DataBuilder(codec, this, property)

        override fun build() = codec
    }

    protected class DataBuilder<P : Any, B : FriendlyByteBuf, T : BaseDataBuilder<P, B>, D>(
        val baseCodec: StreamCodec<in B, D>,
        val previousBuilder: T,
        val property: KMutableProperty1<P, D>
    ) : BaseDataBuilder<P, B>() {
        val codec = object : StreamCodec<B, P> {
            val previousCodec = previousBuilder.build()
            override fun decode(buffer: B): P {
                val returnValue = previousCodec.decode(buffer)
                val value = baseCodec.decode(buffer)
                property.set(returnValue, value)
                return returnValue
            }

            override fun encode(buffer: B, value: P) {
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

        fun <T> custom(codec: StreamCodec<in B, T>, property: KMutableProperty1<P, T>) =
            DataBuilder(codec, this, property)

        override fun build() = codec
    }

    val packetType = CustomPacketPayload.Type<Packet<P, B>>(ResourceLocation.fromNamespaceAndPath(modid, name))

    class Packet<P : Any, B : FriendlyByteBuf>(
        private val base: LibBasePacket<P, B>,
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

    protected fun <T> custom(codec: StreamCodec<in B, T>, property: KMutableProperty1<P, T>) =
        InitialDataBuilder(codec, factory, property)

    protected fun codec(builder: BaseDataBuilder<P, in B>): StreamCodec<B, Packet<P, B>> {
        val self = this
        return object : StreamCodec<B, Packet<P, B>> {
            val handler = self
            val codec = builder.build()

            override fun decode(buffer: B): Packet<P, B> {
                val obj = codec.decode(buffer)
                return Packet(handler, obj)
            }

            override fun encode(buffer: B, value: Packet<P, B>) {
                codec.encode(buffer, value.data)
            }

        }
    }

    open fun onServerReceived(packet: P, context: IPayloadContext) {}

    open fun onClientReceived(packet: P, context: IPayloadContext) {}

    override fun equals(other: Any?): Boolean {
        return other is LibBasePacket<*, *> && name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    fun packet(putData: (dataObj: P) -> Unit): Packet<P, B> {
        val dataObj = factory()
        putData(dataObj)
        return Packet(this, dataObj)
    }
}