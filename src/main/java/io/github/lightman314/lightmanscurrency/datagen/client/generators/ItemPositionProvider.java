package io.github.lightman314.lightmanscurrency.datagen.client.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle2;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.RotationHandler;
import io.github.lightman314.lightmanscurrency.datagen.client.builders.ItemPositionBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class ItemPositionProvider implements DataProvider {

    public static final float MO = 0.0001f;

    private final String name;
    private final String modid;
    private final PackOutput.PathProvider blockPathProvider;
    private final PackOutput.PathProvider positionPathProvider;

    private final Map<Identifier, BlockSet> blockValues = new HashMap<>();
    private final Map<Identifier,ItemPositionBuilder> positionValues = new HashMap<>();

    public ItemPositionProvider(PackOutput output,String modid) { this(output,modid,modid); }
    public ItemPositionProvider(PackOutput output,String modid,String name) {
        this.modid = modid;
        this.name = name;
        this.blockPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK,"lightmanscurrency/item_position_blocks");
        this.positionPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK,"lightmanscurrency/item_position_data");
        RotationHandler.initialize();
    }

    protected abstract void addEntries();

    protected final void addDataWithBlocks(String id,ItemPositionBuilder data,Object... blocks) { this.addDataWithBlocks(Identifier.fromNamespaceAndPath(this.modid,id),data,blocks); }
    protected final void addDataWithBlocks(Identifier id,ItemPositionBuilder data,Object... blocks) {
        this.addData(id,data);
        this.addBlocks(id,blocks);
    }

    protected final void addData(String id,ItemPositionBuilder data) { this.addData(Identifier.fromNamespaceAndPath(this.modid,id),data); }
    protected final void addData(Identifier id,ItemPositionBuilder data) {
        if(this.positionValues.containsKey(id))
            throw new IllegalArgumentException("Data for '" + id + "' is already defined!");
        this.positionValues.put(id,data);
    }

    protected final void addBlocks(String id,Object... blocks) { this.addBlocks(Identifier.fromNamespaceAndPath(this.modid,id),blocks); }
    protected final void addBlocks(Identifier id,Object... blocks) {
        BlockSet set = this.blockValues.computeIfAbsent(id,x -> new BlockSet());
        for(Object obj : blocks)
            this.collectBlockIDs(obj,set);
    }

    protected void collectBlockIDs(Object obj,SetBuilder builder) {
        if(obj instanceof Block block) {
            builder.acceptBlock(block);
        }
        else if(obj instanceof DeferredHolderBundle<?,?,?> bundle) {
            this.collectBlockIDs(bundle.getAllSorted(),builder);
        }
        else if(obj instanceof DeferredHolderBundle2<?,?,?,?> bundle) {
            for(Object b : bundle.getAllSorted()) {
                if(b instanceof Block block)
                    builder.acceptBlock(block);
            }
        }
        else if(obj instanceof Supplier<?> sup) {
            if(sup.get() instanceof Block block)
                builder.acceptBlock(block);
        }
        else if(obj instanceof TagKey<?> tag && tag.isFor(Registries.BLOCK)) {
            builder.acceptTag(tag.location());
        }
        else if(obj instanceof List<?> list) {
            for(Object o : list)
                this.collectBlockIDs(o,builder);
        }
        else if(obj instanceof Identifier blockID)
            builder.acceptBlock(blockID);
    }

    private BlockSet getSet(Identifier id) { return this.blockValues.computeIfAbsent(id,i -> new BlockSet()); }

    protected final void addBlock(String id, Block block) { this.addBlock(Identifier.fromNamespaceAndPath(this.modid,id),block); }
    protected final void addBlock(Identifier id,Block block) { this.addBlock(id,BuiltInRegistries.BLOCK.getKey(block)); }

    protected final void addBlock(String id,Identifier blockID) { this.addBlock(Identifier.fromNamespaceAndPath(this.modid,id),blockID); }
    protected final void addBlock(Identifier id,Identifier blockID) { this.getSet(id).acceptBlock(blockID); }

    protected final void addBlockTag(String id, TagKey<Block> blockTag) { this.addBlockTag(Identifier.fromNamespaceAndPath(this.modid,id),blockTag); }
    protected final void addBlockTag(Identifier id,TagKey<Block> blockTag) { this.addBlockTag(id,blockTag.location()); }
    protected final void addBlockTag(String id,Identifier blockTag) { this.addBlockTag(Identifier.fromNamespaceAndPath(this.modid,id),blockTag); }
    protected final void addBlockTag(Identifier id,Identifier blockTag) { this.getSet(id).acceptTag(blockTag); }

    protected final void replaceBlocks(String id) { this.replaceBlocks(Identifier.fromNamespaceAndPath(this.modid,id)); }
    protected final void replaceBlocks(Identifier id) { this.getSet(id).replace = true; }

    @Override
    public final CompletableFuture<?> run(CachedOutput cache) {
        this.blockValues.clear();
        this.positionValues.clear();
        this.addEntries();
        List<CompletableFuture<?>> results = new ArrayList<>();
        //Save Item Position Data
        this.positionValues.forEach((id,data) -> {

            JsonObject dataJson = data.write();
            Path path = this.positionPathProvider.json(id);
            if(path == null)
                results.add(CompletableFuture.completedFuture(null));
            else
                results.add(DataProvider.saveStable(cache,dataJson,path));
        });
        //Save Blocks
        this.blockValues.forEach((id,set) -> {
            JsonObject root = new JsonObject();
            JsonArray values = new JsonArray();
            if(set.replace)
                root.addProperty("replace",true);
            for(String block : set.set)
                values.add(block);
            root.add("values",values);
            Path path = this.blockPathProvider.json(id);
            if(path == null)
                results.add(CompletableFuture.completedFuture(null));
            else
                results.add(DataProvider.saveStable(cache,root,path));
        });
        return CompletableFuture.allOf(results.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() { return "Lightman's Currency Item Positions: " + this.name; }

    protected interface SetBuilder {
        default void acceptBlock(Block block) { this.acceptBlock(BuiltInRegistries.BLOCK.getKey(block)); }
        void acceptBlock(Identifier blockID);
        default void acceptTag(TagKey<Block> tag) { this.acceptTag(tag.location()); }
        void acceptTag(Identifier tagID);
    }

    private static class BlockSet implements SetBuilder {
        private boolean replace = false;
        private final Set<String> set = new HashSet<>();

        @Override
        public void acceptBlock(Identifier identifier) { this.set.add(identifier.toString()); }
        @Override
        public void acceptTag(Identifier tagID) { this.set.add("#" + tagID); }

    }

}
