package io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ItemPositionSetManager extends SimplePreparableReloadListener<Map<Identifier,List<Predicate<Block>>>> {

    public static final ItemPositionSetManager INSTANCE = new ItemPositionSetManager();

    @Nullable
    public static Identifier getResourceForBlock(BlockState state) { return getResourceForBlock(state == null ? null : state.getBlock()); }
    @Nullable
    public static Identifier getResourceForBlock(Block block) {
        if(block == null)
            return null;
        Identifier blockID = BuiltInRegistries.BLOCK.getKey(block);
        if(INSTANCE.cachedResults.containsKey(blockID))
            return INSTANCE.cachedResults.get(blockID);
        for(var entry : INSTANCE.data.entrySet()) {
            if(isInList(entry.getValue(),block))
            {
                INSTANCE.cachedResults.put(blockID,entry.getKey());
                return entry.getKey();
            }
        }
        INSTANCE.cachedResults.put(blockID,null);
        return null;
    }

    public static ItemPositionData getDataForBlock(BlockState state) { return getDataForBlock(state == null ? null : state.getBlock()); }
    public static ItemPositionData getDataForBlock(Block block) {
        Identifier dataID = getResourceForBlock(block);
        if(dataID != null)
            return ItemPositionManager.getDataOrEmpty(dataID);
        return ItemPositionData.EMPTY;
    }

    private final Map<Identifier,List<Predicate<Block>>> data = new HashMap<>();
    private final Map<Identifier,Identifier> cachedResults = new HashMap<>();

    private final FileToIdConverter idConverter = FileToIdConverter.json("lightmanscurrency/item_position_blocks");
    private ItemPositionSetManager() { }

    @Override
    protected Map<Identifier,List<Predicate<Block>>> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier,List<Predicate<Block>>> preparations = new HashMap<>();
        for(var entry : this.idConverter.listMatchingResourceStacks(manager).entrySet()) {
            Identifier location = entry.getKey();
            Identifier id = this.idConverter.fileToId(location);
            List<Predicate<Block>> list = new ArrayList<>();
            for(Resource r : entry.getValue()) {
                try {
                    try(BufferedReader reader = r.openAsReader()) {
                        JsonObject json = GsonHelper.convertToJsonObject(JsonParser.parseReader(reader),"top element");
                        //Clear existing values from the list
                        if(GsonHelper.getAsBoolean(json,"replace",false))
                            list.clear();
                        JsonArray valueList = GsonHelper.getAsJsonArray(json,"values");
                        for(int i = 0; i < valueList.size(); ++i) {
                            String value = GsonHelper.convertToString(valueList.get(i),"values[" + i + "]");
                            if(value.startsWith("#"))
                                list.add(new TagPredicate(TagKey.create(Registries.BLOCK,Identifier.parse(value.substring(1)))));
                            else
                                list.add(new BlockPredicate(Identifier.parse(value)));
                        }
                    }
                } catch (IOException | JsonParseException | IdentifierException i) {
                    LightmansCurrency.LogError("Couldn't parse data file '{}' from '{}",id,location,i);
                }
            }
            preparations.put(id,ImmutableList.copyOf(list));
        }
        return preparations;
    }

    @Override
    protected void apply(Map<Identifier, List<Predicate<Block>>> preparations, ResourceManager manager, ProfilerFiller profiler) {
        this.data.clear();
        this.data.putAll(preparations);
        this.cachedResults.clear();
    }

    private record TagPredicate(TagKey<Block> tag) implements Predicate<Block> {
        //So long as the built-in registry holder still exists, we might as well use it
        @Override
        @SuppressWarnings("deprecation")
        public boolean test(Block block) { return block.builtInRegistryHolder().is(this.tag); }
    }

    private record BlockPredicate(Identifier block) implements Predicate<Block> {
        @Override
        public boolean test(Block block) { return BuiltInRegistries.BLOCK.getKey(block).equals(this.block); }
    }

    private static boolean isInList(List<Predicate<Block>> list, Block block) { return list.stream().anyMatch(p -> p.test(block)); }

}