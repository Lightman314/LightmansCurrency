package io.github.lightman314.lightmanscurrency.api.taxes.reference.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxReferenceType;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxableReference;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxable;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public class TaxableTraderReference extends TaxableReference {

    public static final TaxReferenceType TYPE = new TraderType();

    private final long traderID;

    public TaxableTraderReference(long traderID) { super(TYPE); this.traderID = traderID; }

    @Nullable
    @Override
    public ITaxable getTaxable(boolean isClient) { return TraderAPI.getApi().GetTrader(isClient, this.traderID); }

    @Override
    protected boolean matches(TaxableReference otherReference) { return otherReference instanceof TaxableTraderReference ttr && ttr.traderID == this.traderID; }

    private static class TraderType extends TaxReferenceType
    {

        private static final MapCodec<TaxableTraderReference> CODEC = Codec.LONG.xmap(TaxableTraderReference::new,t -> t.traderID).fieldOf("trader");
        private static final StreamCodec<ByteBuf,TaxableTraderReference> STREAM_CODEC = ByteBufCodecs.VAR_LONG.map(TaxableTraderReference::new, t -> t.traderID);

        private TraderType() { super(LightmansCurrency.id( "trader")); }

        @Override
        public TaxableReference load(CompoundTag tag) { return new TaxableTraderReference(tag.getLong("TraderID")); }

        @Override
        public MapCodec<TaxableTraderReference> codec() { return CODEC; }
        @Override
        public StreamCodec<ByteBuf,TaxableTraderReference> streamCodec() { return STREAM_CODEC; }

    }

}
