package io.github.lightman314.lightmanscurrency.api.stats.types;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.builtin.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class MultiMoneyStat extends StatType<MoneyView,MoneyValue> {

    public static final StatType<MoneyView,MoneyValue> TYPE = new MultiMoneyStat();

    private static final MapCodec<MMInstance> MAP_CODEC = MoneyStorage.CODEC.fieldOf("value").xmap(MMInstance::new,s -> s.data);
    private static final StreamCodec<RegistryFriendlyByteBuf,MMInstance> STREAM_CODEC = MoneyStorage.STREAM_CODEC.map(MMInstance::new,s -> s.data);

    private MultiMoneyStat() {}

    @Override
    public Instance<MoneyView,MoneyValue> create() { return new MMInstance(); }

    @Override
    public MapCodec<? extends Instance<MoneyView, MoneyValue>> mapCodec() { return MAP_CODEC; }
    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ? extends Instance<MoneyView, MoneyValue>> streamCodec() { return STREAM_CODEC; }

    protected static class MMInstance extends Instance<MoneyView,MoneyValue>
    {
        private final MoneyStorage data;

        private MMInstance() { this(new MoneyStorage()); }
        private MMInstance(MoneyStorage data) { this.data = data; }

        @Override
        protected StatType<MoneyView,MoneyValue> getType() { return TYPE; }
        @Override
        public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) { this.data.load(tag.getList("Value", Tag.TAG_COMPOUND)); }
        @Override
        public MoneyView get() { return this.data.getStoredMoney(); }
        @Override
        protected void addInternal(MoneyValue addAmount) { this.data.addValue(addAmount); }
        @Override
        public void clear() { this.data.clear(); }
        @Override
        public Object getDisplay() { return this.data.getRandomValueText(); }
    }

}
