package io.github.lightman314.lightmanscurrency.api.loot;

import com.google.common.base.Suppliers;
import io.github.lightman314.lightmanscurrency.api.loot.tiers.ChestPoolLevel;
import io.github.lightman314.lightmanscurrency.api.loot.tiers.EntityPoolLevel;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.IModBusEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;


public abstract class DefaultLootConfigEvent extends Event implements IModBusEvent {

    private String defaultNamespace = "minecraft";
    public final void resetDefaultNamespace() { this.setDefaultNamespace("minecraft"); }
    public final void setDefaultNamespace(String namespace) { this.defaultNamespace = namespace; }
    public final String getDefaultNamespace() { return this.defaultNamespace; }

    private final List<String> entries = new ArrayList<>();
    public final List<String> getEntries() { return Collections.unmodifiableList(this.entries); }

    protected abstract Identifier createEntry(String modid,String entry);

    public final void addVanillaEntry(String entry) throws IdentifierException { this.addEntry("minecraft",entry); }
    public final void addEntry(String modid,String entry) throws IdentifierException { this.forceAddEntry(this.createEntry(modid,entry)); }
    public final void addEntry(String entry) throws IdentifierException { this.addEntry(this.defaultNamespace,entry); }

    public final void forceAddEntry(Identifier entry) { this.forceAdd(entry.toString());}

    protected final void forceAdd(String entry) {
        if(!this.entries.contains(entry))
            this.entries.add(entry);
    }

    public final void removeEntry(Identifier entry) { this.entries.remove(entry.toString()); }

    public static final class Chest extends DefaultLootConfigEvent {

        public static Supplier<List<String>> collect(ChestPoolLevel level) { return Suppliers.memoize(() -> ModLoader.postEventWithReturn(new Chest(level)).getEntries()); }

        private final ChestPoolLevel level;
        public ChestPoolLevel getTier() { return this.level; }

        private Chest(ChestPoolLevel level) { this.level = level; }

        @Override
        protected Identifier createEntry(String modid,String entry) { return Identifier.fromNamespaceAndPath(modid,"chests/" + entry); }

    }

    public static final class Entity extends DefaultLootConfigEvent {

        public static Supplier<List<String>> collect(EntityPoolLevel level) { return Suppliers.memoize(() -> ModLoader.postEventWithReturn(new Entity(level)).getEntries()); }

        private final EntityPoolLevel level;
        public EntityPoolLevel getTier() { return this.level; }

        private Entity(EntityPoolLevel level) { this.level = level; }

        @Override
        protected Identifier createEntry(String modid, String entry) { return Identifier.fromNamespaceAndPath(modid,entry); }

        public void forceAddTag(TagKey<EntityType<?>> tag) { this.forceAddTag(tag.location()); }
        public void forceAddTag(Identifier tag) { this.forceAdd("#" + tag); }
        public void addTag(String tagID) throws IdentifierException { this.forceAddTag(Identifier.fromNamespaceAndPath(this.getDefaultNamespace(),tagID)); }

    }

}