package io.github.lightman314.lightmanscurrency.common.impl;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxable;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxReferenceType;
import io.github.lightman314.lightmanscurrency.common.data.types.TaxDataCache;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TaxAPIImpl extends TaxAPI {

    public TaxAPIImpl() {}

    private final Map<ResourceLocation, TaxReferenceType> referenceTypes = new HashMap<>();

    @Override
    public void RegisterReferenceType(TaxReferenceType type) {
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
    public TaxReferenceType GetReferenceType(ResourceLocation type) { return this.referenceTypes.get(type); }

    @Override
    @Nullable
    public ITaxCollector GetTaxCollector(boolean isClient, long collectorID) { return TaxDataCache.TYPE.get(isClient).getEntry(collectorID); }

    @Override
    public ITaxCollector GetServerTaxCollector(boolean isClient) { return TaxDataCache.TYPE.get(isClient).getServerEntry(); }

    @Override
    public List<ITaxCollector> GetTaxCollectorsFor(ITaxable taxable) { return TaxDataCache.TYPE.get(taxable.isClient()).getAllEntries().stream().filter(e -> e.ShouldTax(taxable)).map(e -> (ITaxCollector)e).toList(); }

    @Override
    public List<ITaxCollector> GetPotentialTaxCollectorsFor(ITaxable taxable) { return TaxDataCache.TYPE.get(taxable.isClient()).getAllEntries().stream().filter(e -> e.IsInArea(taxable)).map(e -> (ITaxCollector)e).toList(); }

    @Override
    public List<ITaxCollector> AcknowledgeTaxCollectors(ITaxable taxable) {
        List<ITaxCollector> taxCollectors = this.GetPotentialTaxCollectorsFor(taxable);
        taxCollectors.forEach(c -> c.AcceptTaxable(taxable));
        return taxCollectors;
    }
}
