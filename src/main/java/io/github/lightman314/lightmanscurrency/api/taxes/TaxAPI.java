package io.github.lightman314.lightmanscurrency.api.taxes;

import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxReferenceType;
import io.github.lightman314.lightmanscurrency.common.impl.TaxAPIImpl;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public abstract class TaxAPI {

    private static TaxAPI instance;
    public static TaxAPI getApi()
    {
        if(instance == null)
            instance = new TaxAPIImpl();
        return instance;
    }

    protected TaxAPI() { if(instance != null)  throw new IllegalCallerException("Cannot create a new TaxAPI instance as one is already present!"); }

    /**
     * Registers the given {@link TaxReferenceType} so that it can be loaded via {@link io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxableReference#load(CompoundTag) TaxableReference#load(CompoundTag)}
     */
    public abstract void RegisterReferenceType(TaxReferenceType type);

    /**
     * Gets the registered {@link TaxReferenceType} for the given id
     */
    @Nullable
    public abstract TaxReferenceType GetReferenceType(ResourceLocation type);

    @Nullable
    public final ITaxCollector GetTaxCollector(IClientTracker context, long collectorID) { return this.GetTaxCollector(context.isClient(),collectorID); }
    @Nullable
    public abstract ITaxCollector GetTaxCollector(boolean isClient, long collectorID);

    public final ITaxCollector GetServerTaxCollector(IClientTracker context) { return this.GetServerTaxCollector(context.isClient()); }
    public abstract ITaxCollector GetServerTaxCollector(boolean isClient);

    /**
     * @deprecated Use context sensitive version {@link #GetTaxCollectorsFor(ITaxableContext)}
     */
    @Deprecated(since = "2.3.0.3")
    public final List<ITaxCollector> GetTaxCollectorsFor(ITaxable taxable) { return this.GetTaxCollectorsFor(ITaxableContext.simpleContext(taxable,false)); }

    /**
     * Gets a list of all tax collectors that are currently active and flagged as taxing the given taxable object<br>
     * Note: A tax collector will not be considered active on a taxable object unless {@link #AcknowledgeTaxCollectors(ITaxable)} is called first
     */
    public abstract List<ITaxCollector> GetTaxCollectorsFor(ITaxableContext context);

    /**
     * Gets a list of all tax collectors that apply to the taxable objects current location
     */
    public abstract List<ITaxCollector> GetPotentialTaxCollectorsFor(ITaxable taxable);

    /**
     * Flags the given taxable object as acknowledging all tax collectors that apply to its current location
     * @return A list of all tax collectors that now apply to this tax collector
     */
    public abstract List<ITaxCollector> AcknowledgeTaxCollectors(ITaxable taxable);

}
