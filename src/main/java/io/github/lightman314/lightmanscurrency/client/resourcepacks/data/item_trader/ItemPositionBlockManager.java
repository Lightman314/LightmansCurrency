package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader;

import com.google.gson.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemPositionBlockManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final ItemPositionBlockManager INSTANCE = new ItemPositionBlockManager();

    private ItemPositionBlockManager() { super(GSON, "lightmanscurrency/item_position_blocks"); }

    @Nullable
    public static ResourceLocation getResourceForBlock(@Nonnull BlockState state) { return getResourceForBlock(state.getBlock()); }
    @Nullable
    public static ResourceLocation getResourceForBlock(@Nonnull Block block) {
        ResourceLocation blockID = ForgeRegistries.BLOCKS.getKey(block);
        for(var d : INSTANCE.data.entrySet())
        {
            if(d.getValue().contains(blockID))
                return d.getKey();
        }
        return null;
    }

    @Nonnull
    public static ItemPositionData getDataForBlock(@Nonnull BlockState state) { return getDataForBlock(state.getBlock()); }
    @Nonnull
    public static ItemPositionData getDataForBlock(@Nonnull Block block) {
        ResourceLocation dataID = getResourceForBlock(block);
        if(dataID != null)
            return ItemPositionManager.getDataOrEmpty(dataID);
        return ItemPositionData.EMPTY;
    }

    private final Map<ResourceLocation,List<ResourceLocation>> data = new HashMap<>();

    @Override
    protected void apply(@Nonnull Map<ResourceLocation, JsonElement> map, @Nonnull ResourceManager resourceManager, @Nonnull ProfilerFiller filler) {
        this.data.clear();
        map.forEach((id,json) -> {
            try {
                JsonObject root = GsonHelper.convertToJsonObject(json, "top element");
                JsonArray valueList = GsonHelper.getAsJsonArray(root, "values");
                List<ResourceLocation> results = new ArrayList<>();
                for(int i = 0; i < valueList.size(); ++i)
                {
                    ResourceLocation rl = new ResourceLocation(GsonHelper.convertToString(valueList.get(i),"values["+i+"]"));
                    if(rl != null)
                        results.add(rl);
                }
                this.data.put(id, results);
            } catch (JsonParseException | IllegalArgumentException | ResourceLocationException exception) {
                LightmansCurrency.LogError("Parsing error loading item position data " + id, exception); }
        });
        LightmansCurrency.LogDebug("Loaded " + this.data.size() + " Item Position Block entries!");
    }

}