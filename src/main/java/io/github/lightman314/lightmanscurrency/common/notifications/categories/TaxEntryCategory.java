package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TaxEntryCategory extends NotificationCategory {

    public static final NotificationCategoryType<TaxEntryCategory> TYPE = new NotificationCategoryType<>(VersionUtil.lcResource("tax_entry"),TaxEntryCategory::new);

    private final long entryID;
    private final Component entryName;

    public TaxEntryCategory(Component entryName, long entryID) { this.entryID = entryID; this.entryName = entryName; }

    public TaxEntryCategory(CompoundTag tag)
    {
        if(tag.contains("EntryName"))
            this.entryName = Component.Serializer.fromJson(tag.getString("EntryName"));
        else
            this.entryName = ModBlocks.TAX_COLLECTOR.get().getName();
        if(tag.contains("TraderID"))
            this.entryID = tag.getLong("TraderID");
        else
            this.entryID = -1;
    }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(ModBlocks.TAX_COLLECTOR); }

    @Override
    public Component getName() { return this.entryName; }

    @Override
    protected NotificationCategoryType<TaxEntryCategory> getType() { return TYPE; }

    @Override
    public boolean matches(NotificationCategory other) {
        if(other instanceof TaxEntryCategory otherTax)
            return otherTax.entryID == this.entryID;
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        compound.putString("EntryName", Component.Serializer.toJson(this.entryName));
        compound.putLong("EntryID", this.entryID);
    }
}
