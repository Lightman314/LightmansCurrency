package io.github.lightman314.lightmanscurrency.api.stats;

import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public abstract class StatType<A,B>
{

    @Nonnull
    public static String getTranslationKey(@Nonnull String statKey) { return "statistic.lightmanscurrency." + statKey; }

    private static final Map<ResourceLocation,StatType<?,?>> REGISTRY = new HashMap<>();
    public static void register(@Nonnull StatType<?,?> type) { REGISTRY.put(type.getID(),type); }
    public static StatType<?,?> getID(@Nonnull ResourceLocation type) { return REGISTRY.get(type); }

    @Nonnull
    public abstract ResourceLocation getID();
    @Nonnull
    public abstract Instance<A,B> create();
    @Nonnull
    public final StatKey<A,B> createKey(@Nonnull String statKey) { return StatKey.create(statKey,this); }

    public abstract static class Instance<A,B> implements IClientTracker {

        private StatTracker parent = null;

        protected Instance() {}

        @Override
        public final boolean isClient() { return this.parent.isClient(); }
        @Nonnull
        protected abstract StatType<A,B> getType();
        @Nonnull
        protected final ResourceLocation getID() { return this.getType().getID(); }
        @Nonnull
        public final CompoundTag save(@Nonnull HolderLookup.Provider lookup)
        {
            CompoundTag tag = new CompoundTag();
            this.saveAdditional(tag, lookup);
            tag.putString("Type", this.getType().getID().toString());
            return tag;
        }
        protected abstract void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup);
        public abstract void load(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup);

        public abstract A get();
        public final void add(@Nonnull B addAmount) {
            this.addInternal(addAmount);
            if(this.parent != null)
                this.parent.setChanged();
        }
        protected abstract void addInternal(@Nonnull B addAmount);
        public abstract void clear();

        public void setParent(@Nonnull StatTracker parent) { this.parent = parent; }

        public abstract Object getDisplay();

        @Nonnull
        public MutableComponent getInfoText(@Nonnull String statKey) { return Component.translatable(getTranslationKey(statKey),this.getDisplay()); }

    }
}
