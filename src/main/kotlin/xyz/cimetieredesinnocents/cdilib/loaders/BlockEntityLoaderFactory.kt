package xyz.cimetieredesinnocents.cdilib.loaders

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

open class BlockEntityLoaderFactory(modid: String) {
    val registry = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, modid)

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun <T : BlockEntity> register(
        name: String,
        blockEntity: (BlockPos, BlockState) -> T,
        block: () -> Block
    ): DeferredHolder<BlockEntityType<*>, BlockEntityType<T>> {
        return registry.register(name) { ->
            BlockEntityType.Builder.of(blockEntity, block()).build(null)
        }
    }

    fun bootstrap(bus: IEventBus) {
        registry.register(bus)
    }
}