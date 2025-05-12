package io.github.lightman314.lightmanscurrency.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.mixinsupport.client.ModelManagerData;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @Unique
    private static ModelManagerData data = null;

    @Inject(at = @At("HEAD"),method = "reload")
    private void reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir)
    {
        data = new ModelManagerData(preparationBarrier,resourceManager,preparationProfiler,reloadProfiler,backgroundExecutor,gameExecutor);
    }

    @WrapMethod(method = "loadBlockModels")
    private static CompletableFuture<Map<ResourceLocation, BlockModel>> loadBlockModels(ResourceManager resourceManager, Executor executor, Operation<CompletableFuture<Map<ResourceLocation, BlockModel>>> original)
    {
        CompletableFuture<Map<ResourceLocation,BlockModel>> cf = original.call(resourceManager,executor);
        if(data == null)
            return cf;
        return cf.thenApplyAsync(map -> {
            ModelVariantDataManager.INSTANCE.reload(data.preparationBarrier(),data.resourceManager(),data.preparationsProfiler(),data.reloadProfiler(),data.backgroundExecutor(),data.gameExecutor());
            return map;
        });
    }

}
