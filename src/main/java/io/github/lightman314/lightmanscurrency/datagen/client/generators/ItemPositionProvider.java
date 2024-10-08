package io.github.lightman314.lightmanscurrency.datagen.client.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.datagen.client.builders.ItemPositionBuilder;
import io.github.lightman314.lightmanscurrency.datagen.util.CustomPathProvider;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public abstract class ItemPositionProvider implements DataProvider {

    public static final float MO = 0.0001f;

    protected final String name;
    protected final DataGenerator output;
    final CustomPathProvider blockPathProvider;
    final CustomPathProvider positionPathProvider;

    protected ItemPositionProvider(@Nonnull DataGenerator output, @Nonnull String modid) { this(output, modid, null); }
    protected ItemPositionProvider(@Nonnull DataGenerator output, @Nonnull String modid, @Nullable String subPack)
    {
        this.blockPathProvider = new CustomPathProvider(output, PackType.CLIENT_RESOURCES, "lightmanscurrency/item_position_blocks", subPack);
        this.positionPathProvider = new CustomPathProvider(output, PackType.CLIENT_RESOURCES, "lightmanscurrency/item_position_data", subPack);
        this.output = output;
        this.name = subPack != null ? modid + " (" + subPack + ")" : modid;
    }

    Map<ResourceLocation, BlockData> blockValues = new HashMap<>();
    Map<ResourceLocation, ItemPositionBuilder> positionValues = new HashMap<>();

    protected abstract void addEntries();

    protected final void addDataWithBlocks(@Nonnull ResourceLocation id, @Nonnull ItemPositionBuilder data, @Nonnull Object... blocks) { this.addDataWithBlocks(id, id, data, blocks); }
    protected final void addDataWithBlocks(@Nonnull ResourceLocation id, @Nonnull ResourceLocation target, @Nonnull ItemPositionBuilder data, @Nonnull Object... blocks)
    {
        this.addData(id, data);
        this.addBlocks(id, target, blocks);
    }

    protected final void addData(@Nonnull ResourceLocation id, @Nonnull ItemPositionBuilder data)
    {
        if(!this.positionValues.containsKey(id))
            this.positionValues.put(id, data);
        else
            throw new IllegalArgumentException("Data for '" + id + "' is already defined!");
    }

    protected final void addBlocks(@Nonnull ResourceLocation id, @Nonnull Object... blocks) { this.addBlocks(id,id,blocks); }
    protected final void addBlocks(@Nonnull ResourceLocation id, @Nonnull ResourceLocation target, @Nonnull Object... blocks)
    {
        for(Object obj : blocks)
        {
            if(obj instanceof RegistryObjectBiBundle<?,?,?> bundle)
            {
                List<?> values = bundle.getAllSorted();
                for(Object b : values)
                {
                    if(b instanceof Block block)
                        this.addBlock(id,target,block);
                }
            }
            else if(obj instanceof RegistryObjectBundle<?,?> bundle)
            {
                List<?> values = bundle.getAllSorted();
                for(Object b : values)
                {
                    if(b instanceof Block block)
                        this.addBlock(id,target,block);
                }
            }
            else if(obj instanceof RegistryObject<?> ro)
            {
                if(ro.get() instanceof Block block)
                    this.addBlock(id,target,block);
            }
            else if(obj instanceof TagKey<?> tag)
            {
                if(tag.isFor(ForgeRegistries.BLOCKS.getRegistryKey()))
                    this.addBlockTag(id,target,tag.cast(ForgeRegistries.BLOCKS.getRegistryKey()).orElse(null));
            }
            else if(obj instanceof List<?> list)
            {
                for(Object b : list)
                    this.addBlocks(id,target,b);
            }
            else if(obj instanceof ResourceLocation blockID)
                this.addBlocks(id,target,blockID);
        }
    }
    protected final void addBlock(@Nonnull ResourceLocation id, @Nonnull Block block) { this.addBlock(id, id, block); }
    protected final void addBlock(@Nonnull ResourceLocation id, @Nonnull ResourceLocation target, @Nonnull Block block) { this.addBlock(id,target,ForgeRegistries.BLOCKS.getKey(block)); }

    protected final void addBlock(@Nonnull ResourceLocation id, @Nonnull ResourceLocation blockID) { this.addBlock(id, id, blockID); }
    protected final void addBlock(@Nonnull ResourceLocation id, @Nonnull ResourceLocation target, @Nonnull ResourceLocation blockID)
    {
        BlockData data = this.blockValues.getOrDefault(id, new BlockData(target));
        if(blockID != null && !data.blocks.contains(blockID.toString()))
            data.blocks.add(blockID.toString());
        this.blockValues.put(id, data);
    }

    protected final void addBlockTag(@Nonnull ResourceLocation id, @Nullable TagKey<Block> blockTag) { this.addBlockTag(id, id, blockTag); }
    protected final void addBlockTag(@Nonnull ResourceLocation id, @Nonnull ResourceLocation target, @Nullable TagKey<Block> blockTag) { this.addBlockTag(id, target, blockTag != null ? blockTag.location() : null); }
    protected final void addBlockTag(@Nonnull ResourceLocation id, @Nullable ResourceLocation blockTag) { this.addBlockTag(id, id, blockTag); }
    protected final void addBlockTag(@Nonnull ResourceLocation id, @Nonnull ResourceLocation target, @Nullable ResourceLocation blockTag) {
        BlockData data = this.blockValues.getOrDefault(id,new BlockData(target));
        if(blockTag != null && !data.blocks.contains("#" + blockTag))
            data.blocks.add("#" + blockTag);
        this.blockValues.put(id,data);
    }

    @Override
    public void run(@Nonnull CachedOutput cache) {
        this.blockValues.clear();
        this.positionValues.clear();
        this.addEntries();
        this.positionValues.forEach((id,data) -> {
            //Save Data
            JsonObject dataJson = data.write();
            Path path = this.positionPathProvider.json(id);
            if(path != null)
            {
                try { DataProvider.saveStable(cache, dataJson, path);
                } catch (IOException e) { throw new RuntimeException(e); }
            }
        });
        this.blockValues.forEach((id,blocks) -> {
            //Save Blocks
            JsonObject blockJson = new JsonObject();
            JsonArray blockList = new JsonArray();
            blockJson.addProperty("target",blocks.target.toString());
            for(String block : blocks.blocks)
                blockList.add(block);
            blockJson.add("values", blockList);
            Path path = this.blockPathProvider.json(id);
            if(path != null)
            {
                try { DataProvider.saveStable(cache, blockJson, path);
                } catch (IOException e) { throw new RuntimeException(e); }
            }
        });
    }

    private static class BlockData {
        public final ResourceLocation target;
        public final List<String> blocks = new ArrayList<>();
        public BlockData(@Nonnull ResourceLocation target) { this.target = target; }
    }

    @Nonnull
    @Override
    public String getName() { return "LightmansCurrency Item Positions: " + this.name; }

}