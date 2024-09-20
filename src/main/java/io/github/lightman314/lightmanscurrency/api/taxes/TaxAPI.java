package io.github.lightman314.lightmanscurrency.api.taxes;

import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxReferenceType;
import io.github.lightman314.lightmanscurrency.common.impl.TaxAPIImpl;
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
     * @deprecated Use {@link #RegisterReferenceType(TaxReferenceType)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.3.1")
    public static void registerReferenceType(@Nonnull TaxReferenceType type) { API.RegisterReferenceType(type); }

    /**
     * Gets the registered {@link TaxReferenceType} for the given id
     */
    @Nullable
    public abstract TaxReferenceType GetReferenceType(@Nonnull ResourceLocation type);

    /**
     * @deprecated Use {@link #GetReferenceType(ResourceLocation)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.3.1")
    @Nullable
    public static TaxReferenceType getReferenceType(@Nonnull ResourceLocation type) { return API.GetReferenceType(type); }

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

    /**
     * @deprecated Use {@link #GetTaxCollectorsFor(ITaxable)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.3.1")
    @Nonnull
    public static List<ITaxCollector> GetActiveTaxCollectorsFor(@Nonnull ITaxable taxable) { return API.GetTaxCollectorsFor(taxable); }
    /**
     * @deprecated Use {@link #GetPotentialTaxCollectorsFor(ITaxable)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.3.1")
    @Nonnull
    public static List<ITaxCollector> GetPossibleTaxCollectorsFor(@Nonnull ITaxable taxable) { return API.GetPotentialTaxCollectorsFor(taxable); }


}