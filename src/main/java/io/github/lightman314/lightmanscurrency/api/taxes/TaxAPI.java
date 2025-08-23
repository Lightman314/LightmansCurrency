package io.github.lightman314.lightmanscurrency.api.taxes;

import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxReferenceType;
import io.github.lightman314.lightmanscurrency.common.impl.TaxAPIImpl;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class TaxAPI {

    public static final TaxAPI API = TaxAPIImpl.INSTANCE;

    /**
     * Registers the given {@link TaxReferenceType} so that it can be loaded via {@link io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxableReference#load(CompoundTag) TaxableReference#load(CompoundTag)}
     */
    public abstract void RegisterReferenceType(@Nonnull TaxReferenceType type);

    /**
     * Gets the registered {@link TaxReferenceType} for the given id
     */
    @Nullable
    public abstract TaxReferenceType GetReferenceType(@Nonnull ResourceLocation type);

    @Nullable
    public final ITaxCollector GetTaxCollector(IClientTracker context, long collectorID) { return this.GetTaxCollector(context.isClient(),collectorID); }
    @Nullable
    public abstract ITaxCollector GetTaxCollector(boolean isClient, long collectorID);

    @Nullable
    public final ITaxCollector GetServerTaxCollector(IClientTracker context) { return this.GetServerTaxCollector(context.isClient()); }
    @Nonnull
    public abstract ITaxCollector GetServerTaxCollector(boolean isClient);

    /**
     * Gets a list of all tax collectors that are currently active and flagged as taxing the given taxable object<br>
     * Note: A tax collector will not be considered active on a taxable object unless {@link #AcknowledgeTaxCollectors(ITaxable)} is called first
     */
    @Nonnull
    public abstract List<ITaxCollector> GetTaxCollectorsFor(@Nonnull ITaxable taxable);

    /**
     * Gets a list of all tax collectors that apply to the taxable objects current location
     */
    @Nonnull
    public abstract List<ITaxCollector> GetPotentialTaxCollectorsFor(@Nonnull ITaxable taxable);

    /**
     * Flags the given taxable object as acknowledging all tax collectors that apply to its current location
     * @return A list of all tax collectors that now apply to this tax collector
     */
    @Nonnull
    public abstract List<ITaxCollector> AcknowledgeTaxCollectors(@Nonnull ITaxable taxable);

}
