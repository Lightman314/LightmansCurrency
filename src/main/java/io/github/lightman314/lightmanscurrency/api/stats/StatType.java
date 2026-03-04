package io.github.lightman314.lightmanscurrency.api.stats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public abstract class StatType<A,B>
{

    public static final Codec<Instance<?,?>> CODEC = LCRegistries.STAT_TYPES.byNameCodec().dispatch(Instance::getType,StatType::mapCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf,StatType.Instance<?,?>> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.STAT_TYPE_KEY)
            .dispatch(Instance::getType,StatType::streamCodec);

    public static String getTranslationKey(String statKey) { return "statistic.lightmanscurrency." + statKey; }

    public abstract Instance<A,B> create();
    
    public final StatKey<A,B> createKey(String statKey) { return StatKey.create(statKey,this); }

    public abstract MapCodec<? extends Instance<A,B>> mapCodec();
    public abstract StreamCodec<? super RegistryFriendlyByteBuf,? extends Instance<A,B>> streamCodec();

    @Override
    public int hashCode() { return LCRegistries.STAT_TYPES.getKey(this).hashCode(); }

    @Override
    public String toString() { return "StatType[" + LCRegistries.STAT_TYPES.getKey(this) + "]"; }

    public abstract static class Instance<A,B> implements IClientTracker {

        private StatTracker parent = null;

        protected Instance() {}

        @Override
        public final boolean isClient() { return this.parent.isClient(); }
        
        protected abstract StatType<A,B> getType();
        
        protected final ResourceLocation getID() { return LCRegistries.STAT_TYPES.getKey(this.getType()); }

        public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {}

        public abstract A get();
        public final void add(B addAmount) {
            this.addInternal(addAmount);
            if(this.parent != null)
                this.parent.setChanged();
        }
        protected abstract void addInternal(B addAmount);
        public abstract void clear();

        public void setParent(StatTracker parent) { this.parent = parent; }

        public abstract Object getDisplay();

        public Component getInfoText(String statKey) { return EasyText.translatable(getTranslationKey(statKey),this.getDisplay()); }

    }
}
