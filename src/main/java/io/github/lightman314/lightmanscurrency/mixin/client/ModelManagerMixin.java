package io.github.lightman314.lightmanscurrency.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.lightman314.lightmanscurrency.client.model.util.VariantModelHelper;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {

    @Unique
    private static ModelManagerData lightmanscurrency$data = null;

    @Inject(at = @At("HEAD"),method = "reload")
    private void reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir)
    {
        //Force the ModelVariant data to reload before the vanilla model manager
        lightmanscurrency$data = new ModelManagerData(preparationBarrier,resourceManager,preparationsProfiler,reloadProfiler,backgroundExecutor,gameExecutor);
    }

    @WrapMethod(method = "loadBlockModels")
    private static CompletableFuture<Map<ResourceLocation,BlockModel>> loadBlockModels(ResourceManager resourceManager, Executor executor, Operation<CompletableFuture<Map<ResourceLocation, BlockModel>>> original)
    {
        CompletableFuture<Map<ResourceLocation,BlockModel>> cf = original.call(resourceManager,executor);
        if(lightmanscurrency$data == null)
            return cf;
        return cf.thenApplyAsync(map -> {
            //Force the ModelVariantData to be loaded at the same time that the original
            ModelVariantDataManager.INSTANCE.reload(lightmanscurrency$data.preparationBarrier(), lightmanscurrency$data.resourceManager(), lightmanscurrency$data.preparationsProfiler(), lightmanscurrency$data.reloadProfiler(), lightmanscurrency$data.backgroundExecutor(), lightmanscurrency$data.gameExecutor());
            //Store the map in an editable format locally so that it can be edited in the loadBlockStates data
            Map<ResourceLocation,BlockModel> editableMap = new HashMap<>(map);
            VariantModelHelper.setModelDataCache(editableMap);
            return editableMap;
        });
    }

}
