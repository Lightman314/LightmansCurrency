package io.github.lightman314.lightmanscurrency.common.notifications.types.taxes;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TaxEntryCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

public class TaxesCollectedNotification extends Notification {

    public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "taxes_collected");

    private MutableComponent taxedName = EasyText.literal("NULL");
    private CoinValue amount = CoinValue.EMPTY;
    private TaxEntryCategory category;

    private TaxesCollectedNotification(MutableComponent taxedName, CoinValue amount, TaxEntryCategory category) { this.taxedName = taxedName; this.amount = amount; this.category = category; }

    public static NonNullSupplier<Notification> create(MutableComponent taxedName, CoinValue amount, TaxEntryCategory category) { return () -> new TaxesCollectedNotification(taxedName, amount, category); }

    public TaxesCollectedNotification(CompoundTag tag) { this.load(tag); }

    @Override
    protected ResourceLocation getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return this.category; }

    @Override
    public MutableComponent getMessage() { return EasyText.translatable("notifications.message.taxes.collected", this.amount.getComponent("NULL"), this.taxedName); }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        compound.putString("TaxedName", Component.Serializer.toJson(this.taxedName));
        compound.put("Amount", this.amount.save());
        compound.put("Category", this.category.save());
    }

    @Override
    protected void loadAdditional(CompoundTag compound) {
        this.taxedName = Component.Serializer.fromJson(compound.getString("TaxedName"));
        this.amount = CoinValue.load(compound.getCompound("Amount"));
        this.category = new TaxEntryCategory(compound.getCompound("Category"));
    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof TaxesCollectedNotification tcn)
            return tcn.taxedName.getString().equals(this.taxedName.getString()) && tcn.amount.equals(this.amount) && tcn.category.matches(this.category);
        return false;
    }

}
