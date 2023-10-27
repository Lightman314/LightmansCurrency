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

    Map<ResourceLocation, List<ResourceLocation>> blockValues = new HashMap<>();
    Map<ResourceLocation, ItemPositionBuilder> positionValues = new HashMap<>();

    protected abstract void addEntries();

    protected final void addDataWithBlocks(@Nonnull ResourceLocation id, @Nonnull ItemPositionBuilder data, @Nonnull Object... blocks)
    {
        this.addData(id, data);
        this.addBlocks(id, blocks);
    }

    protected final void addData(@Nonnull ResourceLocation id, @Nonnull ItemPositionBuilder data)
    {
        if(!this.positionValues.containsKey(id))
            this.positionValues.put(id, data);
        else
            throw new IllegalArgumentException("Data for '" + id + "' is already defined!");
    }

    protected final void addBlocks(@Nonnull ResourceLocation id, @Nonnull Object... blocks)
    {
        for(Object obj : blocks)
        {
            if(obj instanceof RegistryObjectBiBundle<?,?,?> bundle)
            {
                List<?> values = bundle.getAllSorted();
                for(Object b : values)
                {
                    if(b instanceof Block block)
                        this.addBlock(id,block);
                }
            }
            else if(obj instanceof RegistryObjectBundle<?,?> bundle)
            {
                List<?> values = bundle.getAllSorted();
                for(Object b : values)
                {
                    if(b instanceof Block block)
                        this.addBlock(id,block);
                }
            }
            else if(obj instanceof RegistryObject<?> ro)
            {
                if(ro.get() instanceof Block block)
                    this.addBlock(id, block);
            }
            else if(obj instanceof List<?> list)
            {
                for(Object b : list)
                {
                    if(b instanceof Block block)
                        this.addBlock(id, block);
                    else if(b instanceof ResourceLocation blockID)
                        this.addBlock(id, blockID);
                }
            }
            else if(obj instanceof ResourceLocation blockID)
                this.addBlocks(id, blockID);
        }
    }
    protected final void addBlock(@Nonnull ResourceLocation id, @Nonnull Block block) { this.addBlock(id, ForgeRegistries.BLOCKS.getKey(block)); }

    protected final void addBlock(@Nonnull ResourceLocation id, @Nonnull ResourceLocation blockID)
    {
        List<ResourceLocation> list = this.blockValues.getOrDefault(id, new ArrayList<>());
        if(blockID != null && !list.contains(blockID))
            list.add(blockID);
        this.blockValues.put(id, list);
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
            for(ResourceLocation block : blocks)
                blockList.add(block.toString());
            blockJson.add("values", blockList);
            Path path = this.blockPathProvider.json(id);
            if(path != null)
            {
                try { DataProvider.saveStable(cache, blockJson, path);
                } catch (IOException e) { throw new RuntimeException(e); }
            }
        });
    }

    @Nonnull
    @Override
    public String getName() { return "LightmansCurrency Item Positions: " + this.name; }

}