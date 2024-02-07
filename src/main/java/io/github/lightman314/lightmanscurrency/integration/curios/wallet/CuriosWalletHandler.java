package io.github.lightman314.lightmanscurrency.integration.curios.wallet;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public record CuriosWalletHandler(LivingEntity entity) implements IWalletHandler {

    @Override
    public ItemStack getWallet() { return LCCurios.getCuriosWalletContents(this.entity); }

    @Override
    public void setWallet(ItemStack walletStack) { }

    @Override
    public void syncWallet(ItemStack walletStack) { }

    @Override
    public boolean visible() { return LCCurios.getCuriosWalletVisibility(this.entity); }

    @Override
    public void setVisible(boolean visible) { }

    @Override
    public boolean isDirty() { return false; }

    @Override
    public void clean() { }

    @Override
    public void tick() { }

    @Override
    public CompoundTag save() { return new CompoundTag(); }

    @Override
    public void load(CompoundTag tag) { }

    @Nonnull
    @Override
    public MoneyView getStoredMoney() { return MoneyView.empty(); }

    public boolean hasStoredMoneyChanged(@Nullable Object context) { return false; }

    @Override
    public void flagAsKnown(@Nullable Object context) {}

    @Override
    public void forgetContext(@Nonnull Object context) { }

}
