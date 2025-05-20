package io.github.lightman314.lightmanscurrency.mixin.client;

import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(AtlasSet.class)
public class AtlasSetMixin {

    @Unique
    protected AtlasSet lightmanscurrency$self() { return (AtlasSet)(Object)this; }

    @Inject(at = @At("RETURN"), method = "scheduleLoad")
    public void onScheduleLoad(ResourceManager resourceManager, int mipLevel, Executor executor, CallbackInfoReturnable<Map<ResourceLocation, CompletableFuture<AtlasSet.StitchResult>>> cir) {
        if(Minecraft.getInstance().getModelManager() instanceof ModelManagerAccessor accessor && this.lightmanscurrency$self() == accessor.getAtlases()) {
            ModelVariantDataManager.atlasPreparation = cir.getReturnValue();
        }
    }

}
