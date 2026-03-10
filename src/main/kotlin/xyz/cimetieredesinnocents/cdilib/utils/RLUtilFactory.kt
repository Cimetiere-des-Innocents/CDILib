package xyz.cimetieredesinnocents.cdilib.utils

import net.minecraft.resources.ResourceLocation

open class RLUtilFactory(val id: String) {
    fun of(name: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(id, name)
    }
}