package io.github.lightman314.lightmanscurrency.api.stats.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class IntegerStat extends StatType<Integer,Integer>
{

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"basic_int");
    public static final StatType<Integer,Integer> INSTANCE = new IntegerStat();

    @Nonnull
    @Override
    public Instance<Integer,Integer> create() { return new IntInstance(); }
    @Nonnull
    @Override
    public ResourceLocation getID() { return TYPE; }

    private IntegerStat(){}

    protected static class IntInstance extends StatType.Instance<Integer,Integer>
    {
        private int value = 0;
        @Nonnull
        @Override
        protected StatType<Integer,Integer> getType() { return INSTANCE; }
        @Override
        protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) { tag.putInt("Value",this.value); }
        @Override
        public void load(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) { this.value = tag.getInt("Value"); }
        @Override
        public Integer get() { return this.value; }
        @Override
        protected void addInternal(@Nonnull Integer addAmount) { this.value += addAmount; }
        @Override
        public void clear() { this.value = 0; }
        @Override
        public Object getDisplay() { return this.value; }
    }

}
