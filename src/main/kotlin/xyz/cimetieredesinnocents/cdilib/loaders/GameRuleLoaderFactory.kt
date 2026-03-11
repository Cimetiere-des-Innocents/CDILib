package xyz.cimetieredesinnocents.cdilib.loaders

import net.minecraft.world.level.GameRules
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import kotlin.reflect.KProperty

open class GameRuleLoaderFactory(private val modid: String) {
    class RegistryItem<T : GameRules.Value<T>>(
        private val name: String,
        private val category: GameRules.Category,
        private val default: GameRules.Type<T>
    ) {
        private lateinit var key: GameRules.Key<T>
        operator fun getValue(thisRef: Any?, propertyKey: KProperty<*>): GameRules.Key<T> {
            return key
        }
        fun register(modid: String) {
            key = GameRules.register("${modid}.${name}", category, default)
        }
    }

    private val registry = mutableListOf<RegistryItem<*>>()

    fun registerBool(name: String, category: GameRules.Category, default: Boolean): RegistryItem<GameRules.BooleanValue> {
        val item = RegistryItem(name, category, GameRules.BooleanValue.create(default))
        registry.add(item)
        return item
    }

    fun registerInt(name: String, category: GameRules.Category, default: Int): RegistryItem<GameRules.IntegerValue> {
        val item = RegistryItem(name, category, GameRules.IntegerValue.create(default))
        registry.add(item)
        return item
    }

    fun bootstrap(bus: IEventBus) {
        bus.addListener(FMLCommonSetupEvent::class.java) {
            it.enqueueWork {
                for (item in registry) {
                    item.register(modid)
                }
            }
        }
    }
}