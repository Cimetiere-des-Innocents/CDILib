package xyz.cimetieredesinnocents.cdilib.loaders

import com.mojang.serialization.Codec
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries

open class DataAttachmentLoaderFactory(modid: String) {
    private val registry = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, modid)

    fun registerInt(name: String, default: Int = 0, copyOnDeath: Boolean = false): DeferredHolder<AttachmentType<*>?, AttachmentType<Int?>> {
        return if (copyOnDeath) {
            registry.register(name) { ->
                AttachmentType.builder { -> default }.serialize(Codec.INT).copyOnDeath().build()
            }
        } else {
            registry.register(name) { -> AttachmentType.builder { -> default }.serialize(Codec.INT).build() }
        }
    }

    fun <T> registerRecord(name: String, codec: Codec<T>, default: () -> T): DeferredHolder<AttachmentType<*>?, AttachmentType<T?>> {
        return registry.register(name) { -> AttachmentType.builder(default).serialize(codec).build() }
    }

    fun <T> registerRecord(name: String, codec: Codec<T>, copyOnDeath: Boolean, default: () -> T): DeferredHolder<AttachmentType<*>?, AttachmentType<T?>> {
        return if (copyOnDeath) {
            registry.register(name) { -> AttachmentType.builder(default).serialize(codec).copyOnDeath().build() }
        } else {
            registry.register(name) { -> AttachmentType.builder(default).serialize(codec).build() }
        }
    }

    fun bootstrap(bus: IEventBus) {
        registry.register(bus)
    }
}