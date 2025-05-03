package io.github.lightman314.lightmanscurrency.mixin.client;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelBakery.class)
public interface ModelBakeryAccessor {

    @Accessor("topLevelModels")
    Map<ModelResourceLocation,UnbakedModel> getTopLevelModels();

    @Accessor("unbakedCache")
    Map<ResourceLocation,UnbakedModel> getUnbakedCache();

}
