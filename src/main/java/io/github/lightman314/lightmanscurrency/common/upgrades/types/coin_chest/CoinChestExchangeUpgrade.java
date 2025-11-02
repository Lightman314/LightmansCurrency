package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest.ExchangeUpgradeTab;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.ATMAPI;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CoinChestExchangeUpgrade extends CoinChestUpgrade {

    public static final int MAX_COMMANDS = 10;

    @Override
    public void HandleMenuMessage(@Nonnull CoinChestMenu menu, @Nonnull CoinChestUpgradeData data, @Nonnull LazyPacketData message)
    {
        if(message.contains("SetExchangeWhileOpen"))
        {
            this.setExchangeWhileOpen(data, message.getBoolean("SetExchangeWhileOpen"));
        }
        if(message.contains("ToggleExchangeCommand"))
        {
            String toggleCommand = message.getString("ToggleExchangeCommand");
            List<String> oldCommands = this.getExchangeCommands(data);

            if(oldCommands.contains(toggleCommand))
                this.removeExchangeCommand(data, toggleCommand);
            else
                this.addExchangeCommand(data,toggleCommand);
        }
    }

    @Override
    public void OnStorageChanged(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data)
    {
        ExecuteExchangeCommand(be, data);
    }

    @Override
    public void OnEquip(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) { ExecuteExchangeCommand(be, data); }

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

    public List<String> getExchangeCommands(CoinChestUpgradeData data)
    {
        CompoundTag compound = data.getItemTag();
        if(compound.contains("ExchangeCommands"))
        {
            ListTag list = compound.getList("ExchangeCommands", Tag.TAG_STRING);
            List<String> result = new ArrayList<>();
            for(int i = 0; i < list.size(); ++i)
                result.add(list.getString(i));
            return result;
        }
        if(compound.contains("ExchangeCommand"))
            return Lists.newArrayList(compound.getString("ExchangeCommand"));
        return new ArrayList<>();
    }

    public void addExchangeCommand(CoinChestUpgradeData data, String newValue)
    {
        CompoundTag compound = data.getItemTag();
        List<String> list = this.getExchangeCommands(data);
        //Don't add if we're already at capacity or if the command is invalid/duplicate
        if(list.contains(newValue) || newValue.isBlank() || list.size() >= MAX_COMMANDS)
            return;
        this.setExchangeCommands(compound,list);
        data.setItemTag(compound);
    }

    public void removeExchangeCommand(CoinChestUpgradeData data, String removedValue)
    {
        CompoundTag compound = data.getItemTag();
        List<String> list = this.getExchangeCommands(data);
        if(list.contains(removedValue))
        {
            list.remove(removedValue);
            this.setExchangeCommands(compound,list);
            data.setItemTag(compound);
        }
    }

    private void setExchangeCommands(CompoundTag tag,List<String> commands)
    {
        ListTag list = new ListTag();
        for(String command : commands)
            list.add(StringTag.valueOf(command));
        tag.put("ExchangeCommands",list);
        if(tag.contains("ExchangeCommand"))
            tag.remove("ExchangeCommand");
    }

    public void ExecuteExchangeCommand(CoinChestBlockEntity be, CoinChestUpgradeData data)
    {
        boolean executeWhileOpen = this.getExchangeWhileOpen(data);
        if(executeWhileOpen || be.getOpenerCount() <= 0)
        {
            List<String> commands = this.getExchangeCommands(data);
            for(String c : commands)
            {
                if(c != null && !c.isBlank())
                {
                    if(ATMAPI.ExecuteATMExchangeCommand(be.getStorage(), c))
                        CoinAPI.getApi().SortCoinsByValue(be.getStorage());
                }
            }
        }
    }

    @Override
    public void addClientTabs(@Nonnull CoinChestUpgradeData data, @Nonnull Object screen, @Nonnull Consumer<Object> consumer) { consumer.accept(new ExchangeUpgradeTab(data, screen)); }

    @Nonnull
    @Override
    public List<Component> getTooltip(@Nonnull UpgradeData data) { return Lists.newArrayList(LCText.TOOLTIP_UPGRADE_COIN_EXCHANGE.get()); }

    @Override
    public boolean clearDataFromStack(@Nonnull ItemStack stack) { return this.clearData(stack, "ExchangeCommand", "ExchangeWhileOpen"); }

}
