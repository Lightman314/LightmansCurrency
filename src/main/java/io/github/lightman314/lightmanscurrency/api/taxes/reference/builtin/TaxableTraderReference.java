package io.github.lightman314.lightmanscurrency.api.taxes.reference.builtin;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxReferenceType;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxableReference;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxable;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class TaxableTraderReference extends TaxableReference {

    public static final TaxReferenceType TYPE = new TraderType();

    private final long traderID;

    public TaxableTraderReference(long traderID) { super(TYPE); this.traderID = traderID; }

    @Nullable
    @Override
    public ITaxable getTaxable(boolean isClient) { return TraderSaveData.GetTrader(isClient, this.traderID); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) { tag.putLong("TraderID", this.traderID); }

    @Override
    protected boolean matches(@Nonnull TaxableReference otherReference) { return otherReference instanceof TaxableTraderReference ttr && ttr.traderID == this.traderID; }

    private static class TraderType extends TaxReferenceType
    {
        private TraderType() { super(new ResourceLocation(LightmansCurrency.MODID, "trader")); }

        @Override
        public TaxableReference load(CompoundTag tag) { return new TaxableTraderReference(tag.getLong("TraderID")); }
    }

}
