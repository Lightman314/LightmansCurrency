package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader;

import com.google.gson.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.BlockTags;
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
import java.util.function.Predicate;

public class ItemPositionBlockManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final ItemPositionBlockManager INSTANCE = new ItemPositionBlockManager();

    private ItemPositionBlockManager() { super(GSON, "lightmanscurrency/item_position_blocks"); }

    @Nullable
    public static ResourceLocation getResourceForBlock(@Nonnull BlockState state) { return getResourceForBlock(state.getBlock()); }
    @Nullable
    public static ResourceLocation getResourceForBlock(@Nonnull Block block) {
        ResourceLocation blockID = ForgeRegistries.BLOCKS.getKey(block);
        for(var d : INSTANCE.data.values())
        {
            if(d.isInList(block))
                return d.target;
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

    private final Map<ResourceLocation,BlockEntry> data = new HashMap<>();

    @Override
    protected void apply(@Nonnull Map<ResourceLocation, JsonElement> map, @Nonnull ResourceManager resourceManager, @Nonnull ProfilerFiller filler) {
        this.data.clear();
        map.forEach((id,json) -> {
            try {
                JsonObject root = GsonHelper.convertToJsonObject(json, "top element");
                JsonArray valueList = GsonHelper.getAsJsonArray(root, "values");
                ResourceLocation target = VersionUtil.parseResource(GsonHelper.getAsString(root,"target",id.toString()));
                List<Predicate<Block>> results = new ArrayList<>();
                for(int i = 0; i < valueList.size(); ++i)
                {
                    String value = GsonHelper.convertToString(valueList.get(i),"values[" + i + "]");
                    if(value.startsWith("#"))
                        results.add(new TagPredicate(VersionUtil.parseResource(value.substring(1))));
                    else
                        results.add(new BlockPredicate(VersionUtil.parseResource(value)));
                }
                this.data.put(id, new BlockEntry(target,results));
            } catch (JsonSyntaxException | IllegalArgumentException | ResourceLocationException exception) {
                LightmansCurrency.LogError("Parsing error loading item position data " + id, exception); }
        });
        LightmansCurrency.LogDebug("Loaded " + this.data.size() + " Item Position Block entries!");
    }

    private record BlockEntry(@Nonnull ResourceLocation target, @Nonnull List<Predicate<Block>> list)
    {
        boolean isInList(@Nonnull Block block) { return this.list.stream().anyMatch(p -> p.test(block)); }
    }

    private record TagPredicate(@Nonnull ResourceLocation tag) implements Predicate<Block>
    {
        @Override
        public boolean test(Block block) {
            return ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(this.tag)).stream().anyMatch(b -> b == block);
        }
    }

    private record BlockPredicate(@Nonnull ResourceLocation blockID) implements Predicate<Block>
    {
        @Override
        public boolean test(Block block) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            return id.equals(this.blockID);
        }
    }

}
