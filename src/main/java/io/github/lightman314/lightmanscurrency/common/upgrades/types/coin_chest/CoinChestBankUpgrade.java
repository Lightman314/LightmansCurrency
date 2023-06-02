package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class CoinChestBankUpgrade extends CoinChestUpgrade {

    @Override
    public void HandleMenuMessage(CoinChestMenu menu, CoinChestUpgradeData data, LazyPacketData message)
    {
        if(message.contains("SetBankAccount"))
        {
            BankAccount.AccountReference account = BankAccount.LoadReference(menu.be.isClient(), message.getNBT("SetBankAccount"));
            this.setBankAccount(data, account);
        }
        if(message.contains("ClearBankAccount"))
        {
            this.setBankAccount(data, null);
        }
        if(message.contains("SetIsDeposit"))
        {
            this.setShouldDeposit(data, message.getBoolean("SetIsDeposit"));
        }
    }

    @Override
    public void OnStorageChanged(CoinChestBlockEntity be, CoinChestUpgradeData data) { this.ExecuteBankInteraction(be, data); }

    @Override
    public void OnEquip(CoinChestBlockEntity be, CoinChestUpgradeData data) { this.ExecuteBankInteraction(be, data); }

    public boolean getShouldDeposit(CoinChestUpgradeData data)
    {
        CompoundTag compound = data.getItemTag();
        if(compound.contains("IsDeposit"))
            return compound.getBoolean("IsDeposit");
        return true;
    }

    public void setShouldDeposit(CoinChestUpgradeData data, boolean newValue)
    {
        CompoundTag compound = data.getItemTag();
        compound.putBoolean("IsDeposit", newValue);
        data.setItemTag(compound);
    }

    @Nullable
    public BankAccount.AccountReference getBankAccount(boolean isClient, CoinChestUpgradeData data)
    {
        CompoundTag compound = data.getItemTag();
        if(compound.contains("BankAccount"))
            return BankAccount.LoadReference(isClient, compound.getCompound("BankAccount"));
        return null;
    }

    public void setBankAccount(CoinChestUpgradeData data, BankAccount.AccountReference newValue)
    {
        CompoundTag compound = data.getItemTag();
        if(newValue == null)
            compound.remove("BankAccount");
        else
            compound.put("BankAccount", newValue.save());
        data.setItemTag(compound);
    }

    public CoinValue getTargetAmount(CoinChestUpgradeData data)
    {
        CompoundTag compound = data.getItemTag();
        if(compound.contains("TargetAmount"))
            return CoinValue.from(compound, "TargetAmount");
        return new CoinValue();
    }

    public void setTargetAmount(CoinChestUpgradeData data, CoinValue newValue)
    {
        CompoundTag compound = data.getItemTag();
        newValue.save(compound, "TargetAmount");
        data.setItemTag(compound);
    }

    public void ExecuteBankInteraction(CoinChestBlockEntity be, CoinChestUpgradeData data)
    {
        //TODO auto-deposit or withdraw from bank account
    }

    @Override
    public void addClientTabs(@Nonnull CoinChestUpgradeData data, @Nonnull Object screen, @Nonnull Consumer<Object> consumer) {
        //TODO make client tab
    }

    @Override
    public List<Component> getTooltip(UpgradeData data) { return Lists.newArrayList(EasyText.translatable("tooltip.lightmanscurrency.upgrade.coin_chest.exchange")); }

    @Override
    public boolean clearDataFromStack(CompoundTag itemTag) { return this.clearTags(itemTag, "IsDeposit", "BankAccount", "TargetAmount"); }

}