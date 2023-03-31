package io.github.lightman314.lightmanscurrency.common.events;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager.PoolLevel;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used for collecting the default config entries for each tier of coin drops
 */
public abstract class DroplistConfigEvent extends Event {

    public static List<String> CollectDefaultEntityDrops(PoolLevel level)
    {
        Entity event = new Entity(level);
        MinecraftForge.EVENT_BUS.post(event);
        return event.collectEntries();
    }

    public static List<String> CollectDefaultChestDrops(PoolLevel level)
    {
        if(level.level > PoolLevel.T6.level)
        {
            LightmansCurrency.LogError("Cannot collect boos-level config entries for a Chest input.");
            return new ArrayList<>();
        }
        Chest event = new Chest(level);
        MinecraftForge.EVENT_BUS.post(event);
        return event.collectEntries();
    }

    private String defaultNamespace = "minecraft";
    public final void resetDefaultNamespace() { this.defaultNamespace = "minecraft"; }
    public final void setDefaultNamespace(@Nonnull String namespace) { this.defaultNamespace = namespace; }
    public final String getDefaultNamespace() { return this.defaultNamespace; }

    private final PoolLevel level;
    public final PoolLevel getTier() { return this.level; }

    private final List<ResourceLocation> entries = new ArrayList<>();
    public final ImmutableList<ResourceLocation> getEntries() { return ImmutableList.copyOf(this.entries); }
    protected final List<String> collectEntries() { return this.entries.stream().map(ResourceLocation::toString).collect(Collectors.toList()); }

    protected DroplistConfigEvent(PoolLevel level) { this.level = level; }

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
     * Adds entry with the given entry name, and the default namespace defined in DroplistConfigEvent.setDefaultNamespace(String)
     * Entry may be modified by the event if the resource used generally is in a sub-folder.
     * (Example: input of "minecraft","zombie" would be turned into the resource location "minecraft:chests/zombie" for the Chest variant of this event)
     * If you don't want the resource to be modified, use DroplistConfigEvent.forceAddEntry(ResourceLocation) instead.
     */
    public final void addEntry(String entry) throws ResourceLocationException { this.addEntry(this.defaultNamespace, entry); }
    public final void forceAddEntry(ResourceLocation entry) {
        if(!this.entries.contains(entry))
            this.entries.add(entry);
    }

    /**
     * Forcibly removes the defined entry from the entry list.
     * Should generally only be useful for adventure map makers that want to define their own coin drop rules, or for other mods overriding my own default values for their mods entities.
     */
    public final void removeEntry(ResourceLocation entry) { this.entries.remove(entry); }

    public static class Chest extends DroplistConfigEvent
    {

        protected Chest(PoolLevel level) { super(level); }

        @Override
        protected ResourceLocation createEntry(String modid, String entry) { return new ResourceLocation(modid, "chests/" + entry); }

    }

    public static class Entity extends DroplistConfigEvent
    {

        protected Entity(PoolLevel level) { super(level); }

        @Override
        protected ResourceLocation createEntry(String modid, String entry) { return new ResourceLocation(modid, entry); }

    }

}