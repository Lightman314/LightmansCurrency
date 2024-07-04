package io.github.lightman314.lightmanscurrency.common.taxes.data;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxable;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxableReference;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class TaxStats {

    private final TaxEntry entry;
    public TaxStats(TaxEntry entry) { this.entry = entry; }


    public void markDirty() { this.entry.markStatsDirty(); }

    private final MoneyStorage totalCollected = new MoneyStorage(this::markDirty);
    public MoneyStorage getTotalCollected() { return this.totalCollected; }

    private final List<CollectionData> collectionCount = new ArrayList<>();

    private int uniqueTaxableCount = 0;
    public int getUniqueTaxableCount() { return this.uniqueTaxableCount; }
    private CollectionData mostTaxed = null;
    @Nullable
    public TaxableReference getMostTaxed() { this.removeInvalidData(); return this.mostTaxed == null ? null : this.mostTaxed.reference; }
    public int getMostTaxedCount() { return this.mostTaxed == null ? 0 : this.mostTaxed.interactionCount; }

    public void removeInvalidData()
    {
        if(this.removeInvalidDataInternal())
            this.markDirty();
    }
    private boolean removeInvalidDataInternal()
    {
        boolean changed = false;
        for(int i = 0; i < this.collectionCount.size(); ++i)
        {
            CollectionData data = this.collectionCount.get(i);
            if(data.reference == null || !data.reference.stillValid(this.entry.isClient()))
            {
                if(this.mostTaxed.equals(data))
                    this.mostTaxed = null;
                this.collectionCount.remove(i--);
                changed = true;
            }
        }
        if(this.mostTaxed == null && changed)
            this.recalculateMostTaxed();
        return changed;
    }

    private void recalculateMostTaxed()
    {
        CollectionData mostTaxed = null;
        int count = 0;
        for(CollectionData data : this.collectionCount)
        {
            if(data.interactionCount > count)
            {
                mostTaxed = data;
                count = data.interactionCount;
            }
        }
        if(mostTaxed != null)
        {
            this.mostTaxed = mostTaxed.copy();
        }
    }

    public void OnTaxesCollected(@Nonnull ITaxable taxable, @Nonnull MoneyValue collectedAmount)
    {
        this.removeInvalidDataInternal();
        this.totalCollected.addValue(collectedAmount);
        TaxableReference reference = taxable.getReference();
        if(reference != null)
        {
            CollectionData data = this.getDataEntry(reference);
            if(data != null)
                data.interactionCount++;
            else
            {
                this.uniqueTaxableCount++;
                data = new CollectionData(reference, 1);
                this.collectionCount.add(data);
            }

            //Check if the new interaction is now the most taxed entry
            if(this.mostTaxed == null || data.interactionCount > this.mostTaxed.interactionCount)
                this.mostTaxed = data.copy();
        }
        this.markDirty();
    }

    @Nullable
    private CollectionData getDataEntry(@Nonnull TaxableReference taxable)
    {
        for(CollectionData data : this.collectionCount)
        {
            if(data.reference != null && data.reference.equals(taxable))
                return data;
        }
        return null;
    }

    public void clear()
    {
        this.totalCollected.clear();
        this.collectionCount.clear();
        this.uniqueTaxableCount = 0;
        this.mostTaxed = null;
        this.markDirty();
    }

    public CompoundTag save(@Nonnull HolderLookup.Provider lookup)
    {
        CompoundTag tag = new CompoundTag();
        tag.put("TotalCollected", this.totalCollected.save());

        ListTag taxableDataList = new ListTag();
        for(CollectionData data : this.collectionCount)
        {
            if(data.reference != null)
                taxableDataList.add(data.save());
        }
        tag.put("TaxableInteractions", taxableDataList);

        tag.putInt("UniqueTaxables", this.uniqueTaxableCount);

        if(this.mostTaxed != null)
            tag.put("MostTaxed", this.mostTaxed.save());

        return tag;
    }

    public void load(CompoundTag tag, @Nonnull HolderLookup.Provider lookup)
    {
        if(tag.contains("TotalCollected"))
            this.totalCollected.safeLoad(tag, "TotalCollected");
        if(tag.contains("TaxableInteractions"))
        {
            this.collectionCount.clear();
            ListTag taxableDataList = tag.getList("TaxableInteractions", Tag.TAG_COMPOUND);
            for(int i = 0; i < taxableDataList.size(); ++i)
            {
                CollectionData d = new CollectionData(taxableDataList.getCompound(i));
                if(d.reference != null)
                {
                    CollectionData d2 = this.getDataEntry(d.reference);
                    if(d2 != null) //Merge duplicate
                        d2.interactionCount += d.interactionCount;
                    else
                        this.collectionCount.add(d);
                }
            }
        }
        if(tag.contains("UniqueTaxables"))
            this.uniqueTaxableCount = tag.getInt("UniqueTaxables");
        if(tag.contains("MostTaxed"))
            this.mostTaxed = new CollectionData(tag.getCompound("MostTaxed"));
        else
            this.mostTaxed = null;
    }

    private static class CollectionData
    {
        public final TaxableReference reference;
        public int interactionCount;
        public CollectionData(TaxableReference reference, int interactionCount) { this.reference = reference; this.interactionCount = interactionCount; }
        public CollectionData(CompoundTag tag) {
            this.reference = TaxableReference.load(tag.getCompound("Taxable"));
            this.interactionCount = tag.getInt("Count");
        }

        public CompoundTag save()
        {
            CompoundTag tag = new CompoundTag();
            if(this.reference == null)
                return tag;
            tag.put("Taxable", this.reference.save());
            tag.putInt("Count", this.interactionCount);
            return tag;
        }

        public CollectionData copy() { return new CollectionData(this.reference, this.interactionCount); }

    }

}
