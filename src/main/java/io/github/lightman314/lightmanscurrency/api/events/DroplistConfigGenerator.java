package io.github.lightman314.lightmanscurrency.api.events;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.loot.tiers.*;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Used for collecting the default config entries for each tier of coin drops.<br>
 * Note: This is called when my mod is first loaded, so in order to register your own listeners
 * you will need to make sure your mod loads before mine.
 */
public abstract class DroplistConfigGenerator {

    private final static List<Consumer<Entity>> entityListeners = new ArrayList<>();
    public static void registerEntityListener(@Nonnull Consumer<Entity> listener) { if(!entityListeners.contains(listener)) entityListeners.add(listener); }
    private final static List<Consumer<Chest>> chestListeners = new ArrayList<>();
    public static void registerChestListener(@Nonnull Consumer<Chest> listener) { if(!chestListeners.contains(listener)) chestListeners.add(listener); }

    public static List<String> CollectDefaultEntityDrops(EntityPoolLevel level)
    {
        Entity generator = new Entity(level);
        for(Consumer<Entity> listener : entityListeners)
        {
            try { listener.accept(generator);
            } catch(Throwable t) { LightmansCurrency.LogError("Error collecting default entity drops.", t); }
        }
        return debugEntries(generator.getEntries(), "Collected Default Entity drops of type '" + level.toString() + "'!\n_VALUE_");
    }

    public static List<String> CollectDefaultChestDrops(ChestPoolLevel level)
    {
        Chest generator = new Chest(level);
        for(Consumer<Chest> listener : chestListeners)
        {
            try { listener.accept(generator);
            } catch(Throwable t) { LightmansCurrency.LogError("Error collecting default chest drops.", t); }
        }
        return debugEntries(generator.getEntries(), "Collected Default Chest drops of type '" + level.toString() + "'!\n_VALUE_");
    }

    private static List<String> debugEntries(List<String> results, String message)
    {
        StringBuilder builder = new StringBuilder("[");
        for(String result : results)
        {
            if(results.indexOf(result) > 0)
                builder.append(",");
            builder.append('"').append(result).append('"');
        }
        builder.append(']');

        LightmansCurrency.LogDebug(message.replace("_VALUE_", builder.toString()));
        return results;
    }

    private String defaultNamespace = "minecraft";
    public final void resetDefaultNamespace() { this.defaultNamespace = "minecraft"; }
    public final void setDefaultNamespace(@Nonnull String namespace) { this.defaultNamespace = namespace; }
    public final String getDefaultNamespace() { return this.defaultNamespace; }

    private final List<String> entries = new ArrayList<>();
    public final ImmutableList<String> getEntries() { return ImmutableList.copyOf(this.entries); }

    protected DroplistConfigGenerator() { }

    protected abstract ResourceLocation createEntry(String modid, String entry);

    /**
     * Adds entry with the given entry name and the "minecraft" namespace.
     * Entry may be modified by the event if the resource used generally is in a sub-folder.
     * (Example: input of "minecraft","zombie" would be turned into the resource location "minecraft:chests/zombie" for the Chest variant of this event)
     * If you don't want the resource to be modified, use DroplistConfigEvent.forceAddEntry(ResourceLocation) instead.
     */
    public final void addVanillaEntry(String entry) throws ResourceLocationException { this.addEntry("minecraft", entry); }

    /**
     * Adds entry with the given namespace and entry name.
     * Entry may be modified by the event if the resource used generally is in a sub-folder.
     * (Example: input of "minecraft","zombie" would be turned into the resource location "minecraft:chests/zombie" for the Chest variant of this event)
     * If you don't want the resource to be modified, use DroplistConfigEvent.forceAddEntry(ResourceLocation) instead.
     */
    public final void addEntry(String modid, String entry) throws ResourceLocationException { this.forceAddEntry(this.createEntry(modid, entry)); }

    /**
     * Adds entry with the given entry name, and the default namespace defined in {@link #setDefaultNamespace(String)}
     * Entry may be modified by the event if the resource used generally is in a sub-folder.
     * (Example: input of "minecraft","zombie" would be turned into the resource location "minecraft:chests/zombie" for the Chest variant of this event)
     * If you don't want the resource to be modified, use DroplistConfigEvent.forceAddEntry(ResourceLocation) instead.
     */
    public final void addEntry(String entry) throws ResourceLocationException { this.addEntry(this.defaultNamespace, entry); }

    public final void forceAddEntry(@Nonnull ResourceLocation entry) { this.forceAdd(entry.toString()); }

    protected final void forceAdd(@Nonnull String entry) {
        if(!this.entries.contains(entry))
            this.entries.add(entry);
    }

    /**
     * Forcibly removes the defined entry from the entry list.
     * Should generally only be useful for adventure map makers that want to define their own coin drop rules, or for other mods overriding my own default values for their mods entities.
     */
    public final void removeEntry(@Nonnull ResourceLocation entry) { this.entries.remove(entry); }

    public static class Chest extends DroplistConfigGenerator
    {

        private final ChestPoolLevel level;
        public final ChestPoolLevel getTier() { return this.level; }

        protected Chest(ChestPoolLevel level) { this.level = level; }

        @Override
        protected ResourceLocation createEntry(String modid, String entry) { return new ResourceLocation(modid, "chests/" + entry); }

    }

    public static class Entity extends DroplistConfigGenerator
    {

        private final EntityPoolLevel level;
        public final EntityPoolLevel getTier() { return this.level; }

        protected Entity(EntityPoolLevel level) { this.level = level; }

        @Override
        protected ResourceLocation createEntry(String modid, String entry) { return new ResourceLocation(modid, entry); }

        public final void forceAddTag(@Nonnull TagKey<EntityType<?>> tag) { this.forceAdd("#" + tag.location()); }
        public final void forceAddTag(@Nonnull ResourceLocation tag) { this.forceAdd("#" + tag); }
        public final void addTag(@Nonnull String tagID) throws ResourceLocationException { this.forceAddTag(new ResourceLocation(this.getDefaultNamespace(), tagID)); }

    }

}
