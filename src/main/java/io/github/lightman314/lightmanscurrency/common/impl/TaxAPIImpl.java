package io.github.lightman314.lightmanscurrency.common.impl;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxable;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxReferenceType;
import io.github.lightman314.lightmanscurrency.common.data.types.TaxDataCache;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxAPIImpl extends TaxAPI {

    public static final TaxAPI INSTANCE = new TaxAPIImpl();

    private TaxAPIImpl() {}

    private final Map<ResourceLocation, TaxReferenceType> referenceTypes = new HashMap<>();

    @Override
    public void RegisterReferenceType(@Nonnull TaxReferenceType type) {
        ResourceLocation id = type.typeID;
        if(this.referenceTypes.containsKey(id))
            LightmansCurrency.LogWarning("Attempted to register the TaxReferenceType '" + id + "' twice!");
        else
        {
            this.referenceTypes.put(id, type);
            LightmansCurrency.LogDebug("Registered TaxReferenceType '" + id + "'!");
        }
    }

    @Nullable
    @Override
    public TaxReferenceType GetReferenceType(@Nonnull ResourceLocation type) { return this.referenceTypes.get(type); }

    @Nonnull
    @Override
    public List<ITaxCollector> GetTaxCollectorsFor(@Nonnull ITaxable taxable) { return TaxDataCache.TYPE.get(taxable).getAllEntries().stream().filter(e -> e.ShouldTax(taxable)).map(e -> (ITaxCollector)e).toList(); }

    @Nonnull
    @Override
    public List<ITaxCollector> GetPotentialTaxCollectorsFor(@Nonnull ITaxable taxable) { return TaxDataCache.TYPE.get(taxable).getAllEntries().stream().filter(e -> e.IsInArea(taxable)).map(e -> (ITaxCollector)e).toList(); }

    @Nonnull
    @Override
    public List<ITaxCollector> AcknowledgeTaxCollectors(@Nonnull ITaxable taxable) {
        List<ITaxCollector> taxCollectors = this.GetPotentialTaxCollectorsFor(taxable);
        taxCollectors.forEach(c -> c.AcceptTaxable(taxable));
        return taxCollectors;
    }

}