package io.github.lightman314.lightmanscurrency.api.stats.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class IntegerStat extends StatType<Integer,Integer>
{

    public static final StatType<Integer,Integer> TYPE = new IntegerStat();

    private static final MapCodec<IntInstance> MAP_CODEC = Codec.INT.xmap(IntInstance::new,IntInstance::get).fieldOf("value");
    private static final StreamCodec<ByteBuf,IntInstance> STREAM_CODEC = ByteBufCodecs.INT.map(IntInstance::new,IntInstance::get);

    @Override
    public Instance<Integer,Integer> create() { return new IntInstance(); }
    @Override
    public MapCodec<? extends Instance<Integer,Integer>> mapCodec() { return MAP_CODEC; }
    @Override
    public StreamCodec<ByteBuf,? extends Instance<Integer,Integer>> streamCodec() { return STREAM_CODEC; }

    private IntegerStat(){}

    protected static class IntInstance extends StatType.Instance<Integer,Integer>
    {
        private int value = 0;
        private IntInstance() {}
        private IntInstance(int value) { this.value = value; }
        @Override
        protected StatType<Integer,Integer> getType() { return TYPE; }
        @Override
        public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) { this.value = tag.getInt("Value"); }
        @Override
        public Integer get() { return this.value; }
        @Override
        protected void addInternal(Integer addAmount) { this.value += addAmount; }
        @Override
        public void clear() { this.value = 0; }
        @Override
        public Object getDisplay() { return this.value; }
    }

}
