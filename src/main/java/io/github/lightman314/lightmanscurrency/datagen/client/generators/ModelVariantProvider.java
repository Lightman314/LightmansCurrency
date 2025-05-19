package io.github.lightman314.lightmanscurrency.datagen.client.generators;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperty;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ModelVariantProvider implements DataProvider {

    //protected static final ResourceLocation EMPTY_MODEL = VersionUtil.lcResource("block/empty");

    protected final String modid;
    private final PackOutput.PathProvider provider;
    protected ModelVariantProvider(PackOutput output,String modid) {
        this.provider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK,ModelVariantDataManager.DIRECTORY);
        this.modid = modid;
    }

    private final Map<ResourceLocation,ModelVariant> data = new HashMap<>();

    protected abstract void addEntries();

    protected final void add(String id, ModelVariant variant) { this.add(VersionUtil.modResource(this.modid,id),variant); }
    protected final void add(ResourceLocation id, ModelVariant variant) { this.data.put(id,variant); }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        this.data.clear();
        this.addEntries();
        List<CompletableFuture<?>> results = new ArrayList<>();
        this.data.forEach((id,variant) -> {
            JsonObject json = variant.write();
            Path path = this.provider.json(id);
            if(path == null)
                results.add(CompletableFuture.completedFuture(null));
            else
                results.add(DataProvider.saveStable(output,json,path));
        });
        return CompletableFuture.allOf(results.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() { return "Lightman's Currency Model Variants: " + this.modid; }

}
