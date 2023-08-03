package io.github.lightman314.lightmanscurrency.common.notifications.types.taxes;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

public class TaxesPaidNotification extends Notification {

    public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "taxes_paid");

    private CoinValue amount = CoinValue.EMPTY;
    private NotificationCategory category = NotificationCategory.GENERAL;
    private TaxesPaidNotification(CoinValue amount, NotificationCategory category) { this.amount = amount; this.category = category;  }

    public static NonNullSupplier<Notification> create(CoinValue amount, NotificationCategory category) { return () -> new TaxesPaidNotification(amount, category); }

    public TaxesPaidNotification(CompoundTag tag) { this.load(tag); }

    @Override
    protected ResourceLocation getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return this.category; }

    @Override
    public MutableComponent getMessage() {
        if(this.amount.hasAny())
            return EasyText.translatable("notifications.message.taxes.paid", this.amount.getComponent("ERROR"));
        else
            return EasyText.translatable("notifications.message.taxes.paid.null");
    }

    @Override
    protected void saveAdditional(CompoundTag compound)
    {
        compound.put("Amount", this.amount.save());
        compound.put("Category", this.category.save());
    }

    @Override
    protected void loadAdditional(CompoundTag compound)
    {
        this.amount = CoinValue.load(compound.getCompound("Amount"));
        this.category = NotificationCategory.deserialize(compound.getCompound("Category"));
    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof TaxesPaidNotification tpn)
            return tpn.amount.equals(this.amount) && tpn.category.matches(this.category);
        return false;
    }
}