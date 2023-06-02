package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest.ExchangeUpgradeTab;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.common.money.ATMUtil;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class CoinChestExchangeUpgrade extends CoinChestUpgrade {


    @Override
    public void HandleMenuMessage(CoinChestMenu menu, CoinChestUpgradeData data, LazyPacketData message)
    {
        if(message.contains("SetExchangeWhileOpen"))
        {
            this.setExchangeWhileOpen(data, message.getBoolean("SetExchangeWhileOpen"));
        }
        if(message.contains("SetExchangeCommand"))
        {
            String newCommand = message.getString("SetExchangeCommand");
            String oldCommand = this.getExchangeCommand(data);

            if(Objects.equals(newCommand, oldCommand))
                this.setExchangeCommand(data, "");
            else
                this.setExchangeCommand(data, newCommand);
        }
    }

    @Override
    public void OnStorageChanged(CoinChestBlockEntity be, CoinChestUpgradeData data)
    {
        ExecuteExchangeCommand(be, data);
    }

    @Override
    public void OnEquip(CoinChestBlockEntity be, CoinChestUpgradeData data) { ExecuteExchangeCommand(be, data); }

    public boolean getExchangeWhileOpen(CoinChestUpgradeData data)
    {
        CompoundTag compound = data.getItemTag();
        if(compound.contains("ExchangeWhileOpen"))
            return compound.getBoolean("ExchangeWhileOpen");
        return true;
    }

    public void setExchangeWhileOpen(CoinChestUpgradeData data, boolean newValue)
    {
        CompoundTag compound = data.getItemTag();
        compound.putBoolean("ExchangeWhileOpen", newValue);
        data.setItemTag(compound);
    }

    public String getExchangeCommand(CoinChestUpgradeData data)
    {
        CompoundTag compound = data.getItemTag();
        if(compound.contains("ExchangeCommand"))
            return compound.getString("ExchangeCommand");
        return "";
    }

    public void setExchangeCommand(CoinChestUpgradeData data, String newValue)
    {
        CompoundTag compound = data.getItemTag();
        compound.putString("ExchangeCommand", newValue);
        data.setItemTag(compound);
    }

    public void ExecuteExchangeCommand(CoinChestBlockEntity be, CoinChestUpgradeData data)
    {
        boolean executeWhileOpen = this.getExchangeWhileOpen(data);
        if(executeWhileOpen || be.getOpenerCount() <= 0)
        {
            String command = this.getExchangeCommand(data);
            if(command != null && !command.isBlank())
            {
                if(ATMUtil.ExecuteATMExchangeCommand(be.getStorage(), command))
                    MoneyUtil.SortCoins(be.getStorage());
            }
        }
    }

    @Override
    public void addClientTabs(@Nonnull CoinChestUpgradeData data, @Nonnull Object screen, @Nonnull Consumer<Object> consumer) { consumer.accept(new ExchangeUpgradeTab(data, screen)); }

    @Override
    public List<Component> getTooltip(UpgradeData data) { return Lists.newArrayList(EasyText.translatable("tooltip.lightmanscurrency.upgrade.coin_chest.exchange")); }

    @Override
    public boolean clearDataFromStack(CompoundTag itemTag) { return this.clearTags(itemTag, "ExchangeCommand", "ExchangeWhileOpen"); }

}