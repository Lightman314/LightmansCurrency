package io.github.lightman314.lightmanscurrency.datagen.client.generators;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.CustomModelData;
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
public abstract class CustomModelDataProvider implements DataProvider {

    protected final PackOutput output;
    protected final String modid;
    private final PackOutput.PathProvider provider;

    public CustomModelDataProvider(PackOutput output,String modid) {
        this.output = output;
        this.modid = modid;
        this.provider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK,"lightmanscurrency/custom_model_data");
    }

    private final Map<ResourceLocation,CustomModelData> data = new HashMap<>();

    protected abstract void addEntries();

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        this.data.clear();
        this.addEntries();
        List<CompletableFuture<?>> results = new ArrayList<>();
        this.data.forEach((id,value) -> {
            //Save data
            JsonObject json = value.write();
            Path path = provider.json(id);
            if(path == null)
                results.add(CompletableFuture.completedFuture(null));
            else
                results.add(DataProvider.saveStable(output,json,path));
        });
        return CompletableFuture.allOf(results.toArray(CompletableFuture[]::new));
    }

    protected final void add(String id, CustomModelData data) { this.add(VersionUtil.modResource(this.modid,id),data); }
    protected final void add(ResourceLocation id, CustomModelData data) { this.data.put(id,data); }

    @Override
    public String getName() { return "Lightman's Currency Custom Models: " + modid; }

}
