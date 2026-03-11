package xyz.cimetieredesinnocents.cdilib.datagen

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.neoforged.neoforge.client.model.generators.ItemModelProvider
import net.neoforged.neoforge.client.model.generators.ModelFile
import net.neoforged.neoforge.client.model.generators.ModelProvider
import xyz.cimetieredesinnocents.cdilib.loaders.DataGenLoaderFactory

abstract class BaseItemModelProvider(modid: String, context: DataGenLoaderFactory.Context) :
    ItemModelProvider(context.output, modid, context.efh) {
    protected fun handheld(item: Item, textureName: String? = null) {
        val name = BuiltInRegistries.ITEM.getKey(item)
        val texturePath = if (textureName != null) {
            ResourceLocation.fromNamespaceAndPath(modid, "${ModelProvider.ITEM_FOLDER}/${textureName}")
        } else {
            ResourceLocation.fromNamespaceAndPath(modid, "${ModelProvider.ITEM_FOLDER}/${name.path}")
        }
        getBuilder(name.toString())
            .parent(ModelFile.UncheckedModelFile("item/handheld"))
            .texture("layer0", texturePath)
    }
}