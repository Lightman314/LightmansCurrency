package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest.ExchangeUpgradeTab;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.ATMAPI;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.data.ExchangeUpgradeData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class CoinChestExchangeUpgrade extends CoinChestUpgrade {

    @Override
    public void HandleMenuMessage(@Nonnull CoinChestMenu menu, @Nonnull CoinChestUpgradeData data, @Nonnull LazyPacketData message) {
        if (message.contains("SetExchangeWhileOpen")) {
            this.setExchangeWhileOpen(data, message.getBoolean("SetExchangeWhileOpen"));
        }
        if (message.contains("SetExchangeCommand")) {
            String newCommand = message.getString("SetExchangeCommand");
            String oldCommand = this.getExchangeCommand(data);

            if (Objects.equals(newCommand, oldCommand))
                this.setExchangeCommand(data, "");
            else
                this.setExchangeCommand(data, newCommand);
        }
    }

    @Override
    public void OnStorageChanged(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) {
        ExecuteExchangeCommand(be, data);
    }

    @Override
    public void OnEquip(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) {
        ExecuteExchangeCommand(be, data);
    }

    public boolean getExchangeWhileOpen(@Nonnull CoinChestUpgradeData data) {
        return data.getData(ModDataComponents.EXCHANGE_UPGRADE_DATA, ExchangeUpgradeData.DEFAULT).exchangeWhileOpen();
    }

    public void setExchangeWhileOpen(@Nonnull CoinChestUpgradeData data, boolean newValue) {
        data.editData(ModDataComponents.EXCHANGE_UPGRADE_DATA, ExchangeUpgradeData.DEFAULT, d -> d.withExchangeWhileOpen(newValue));
    }

    public String getExchangeCommand(@Nonnull CoinChestUpgradeData data) {
        return data.getData(ModDataComponents.EXCHANGE_UPGRADE_DATA, ExchangeUpgradeData.DEFAULT).exchangeCommand();
    }

    public void setExchangeCommand(@Nonnull CoinChestUpgradeData data, @Nonnull String newValue) {
        data.editData(ModDataComponents.EXCHANGE_UPGRADE_DATA, ExchangeUpgradeData.DEFAULT, d -> d.withExchangeCommand(newValue));
    }

    public void ExecuteExchangeCommand(CoinChestBlockEntity be, CoinChestUpgradeData data) {
        boolean executeWhileOpen = this.getExchangeWhileOpen(data);
        if (executeWhileOpen || be.getOpenerCount() <= 0) {
            String command = this.getExchangeCommand(data);
            if (command != null && !command.isBlank()) {
                if (ATMAPI.ExecuteATMExchangeCommand(be.getStorage(), command))
                    CoinAPI.API.SortCoinsByValue(be.getStorage());
            }
        }
    }

    @Override
    public void addClientTabs(@Nonnull CoinChestUpgradeData data, @Nonnull Object screen, @Nonnull Consumer<Object> consumer) {
        consumer.accept(new ExchangeUpgradeTab(data, screen));
    }

    @Nonnull
    @Override
    public List<Component> getTooltip(@Nonnull UpgradeData data) {
        return Lists.newArrayList(LCText.TOOLTIP_UPGRADE_COIN_EXCHANGE.get());
    }

    @Override
    public boolean clearDataFromStack(@Nonnull ItemStack stack) {
        return this.clearData(stack, ModDataComponents.EXCHANGE_UPGRADE_DATA);
    }

}
