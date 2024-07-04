package io.github.lightman314.lightmanscurrency.api.stats.types;

import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class MultiMoneyStat extends StatType<MoneyView,MoneyValue> {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(MoneyAPI.MODID,"multi_money");
    public static final StatType<MoneyView,MoneyValue> INSTANCE = new MultiMoneyStat();

    private MultiMoneyStat() {}

    @Nonnull
    @Override
    public ResourceLocation getID() { return TYPE; }
    @Nonnull
    @Override
    public Instance<MoneyView,MoneyValue> create() { return new MMInstance(); }

    protected static class MMInstance extends Instance<MoneyView,MoneyValue>
    {
        private final MoneyStorage data = new MoneyStorage(() -> {});
        @Nonnull
        @Override
        protected StatType<MoneyView, MoneyValue> getType() { return INSTANCE; }
        @Override
        protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) { tag.put("Value",this.data.save()); }
        @Override
        public void load(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) { this.data.load(tag.getList("Value", Tag.TAG_COMPOUND)); }
        @Override
        public MoneyView get() { return this.data.getStoredMoney(); }
        @Override
        protected void addInternal(@Nonnull MoneyValue addAmount) { this.data.addValue(addAmount); }
        @Override
        public void clear() { this.data.clear(); }
        @Override
        public Object getDisplay() { return this.data.getRandomValueText(); }
    }

}
