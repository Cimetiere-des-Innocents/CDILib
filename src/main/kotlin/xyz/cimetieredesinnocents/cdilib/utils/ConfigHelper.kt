package xyz.cimetieredesinnocents.cdilib.utils

import net.neoforged.neoforge.common.ModConfigSpec
import kotlin.reflect.KProperty

operator fun <T> ModConfigSpec.ConfigValue<T>.getValue(thisRef: Any?, propertyGetter: KProperty<*>): T {
    return get()
}