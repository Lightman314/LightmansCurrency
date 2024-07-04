package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class TaxEntryCategory extends NotificationCategory {

    public static final NotificationCategoryType<TaxEntryCategory> TYPE = new NotificationCategoryType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"tax_entry"),TaxEntryCategory::new);

    private final long entryID;
    private final MutableComponent entryName;
    public MutableComponent getEntryName() { return this.entryName; }

    public TaxEntryCategory(MutableComponent entryName, long entryID) { this.entryID = entryID; this.entryName = entryName; }

    public TaxEntryCategory(CompoundTag tag, @Nonnull HolderLookup.Provider lookup)
    {
        if(tag.contains("EntryName"))
            this.entryName = Component.Serializer.fromJson(tag.getString("EntryName"), lookup);
        else
            this.entryName = ModBlocks.TAX_COLLECTOR.get().getName();
        if(tag.contains("TraderID"))
            this.entryID = tag.getLong("TraderID");
        else
            this.entryID = -1;
    }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(ModBlocks.TAX_COLLECTOR); }

    @Nonnull
    @Override
    public MutableComponent getName() { return this.getEntryName(); }

    @Nonnull
    @Override
    protected NotificationCategoryType<TaxEntryCategory> getType() { return TYPE; }

    @Override
    public boolean matches(NotificationCategory other) {
        if(other instanceof TaxEntryCategory otherTax)
            return otherTax.entryID == this.entryID;
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        compound.putString("EntryName", Component.Serializer.toJson(this.entryName, lookup));
        compound.putLong("EntryID", this.entryID);
    }
}
