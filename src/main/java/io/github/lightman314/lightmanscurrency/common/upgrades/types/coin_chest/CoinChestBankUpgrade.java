package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest.BankUpgradeSelectTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest.BankUpgradeSettingsTab;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.data.BankUpgradeData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class CoinChestBankUpgrade extends TickableCoinChestUpgrade {


    @Override
    public void HandleMenuMessage(@Nonnull CoinChestMenu menu, @Nonnull CoinChestUpgradeData data, @Nonnull LazyPacketData message) {
        if(message.contains("SetDepositMode"))
        {
            this.setDepositMode(data,message.getBoolean("SetDepositMode"));
        }
        if(message.contains("SetMoneyLimit"))
        {
            this.setMoneyLimit(data,message.getMoneyValue("SetMoneyLimit"));
        }
        if(message.contains("SetBankAccount"))
        {
            this.setSelectedBankAccount(data, menu.player, BankReference.load(message.getNBT("SetBankAccount")));
        }
        if(message.contains("CollectOverflowItems"))
        {
            this.clearOverflowItems(menu,data);
        }
    }

    private BankUpgradeData getData(@Nonnull CoinChestUpgradeData data) { return data.getData(ModDataComponents.BANK_UPGRADE_DATA,BankUpgradeData.DEFAULT); }
    private void editData(@Nonnull CoinChestUpgradeData data, @Nonnull UnaryOperator<BankUpgradeData> edit) { data.editData(ModDataComponents.BANK_UPGRADE_DATA,BankUpgradeData.DEFAULT,edit); }

    public boolean isDepositMode(@Nonnull CoinChestUpgradeData data) { return this.getData(data).depositMode; }
    public void setDepositMode(@Nonnull CoinChestUpgradeData data, boolean depositMode) { this.editData(data, d -> d.setDepositMode(depositMode));}

    @Nonnull
    public MoneyValue getMoneyLimit(@Nonnull CoinChestUpgradeData data) { return this.getData(data).moneyLimit; }
    public void setMoneyLimit(@Nonnull CoinChestUpgradeData data, @Nonnull MoneyValue moneyLimit) { this.editData(data,d -> d.setMoneyLimit(moneyLimit)); }

    @Nullable
    public BankReference getTargetAccount(@Nonnull CoinChestUpgradeData data) { return this.getData(data).targetAccount; }
    @Nullable
    public IBankAccount getSelectedBankAccount(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data)
    {
        BankUpgradeData d = this.getData(data);
        if(d.targetAccount != null && d.player != null && d.targetAccount.allowedAccess(d.player))
            return d.targetAccount.flagAsClient(be).get();
        return null;
    }

    public void setSelectedBankAccount(@Nonnull CoinChestUpgradeData data, @Nonnull Player player, @Nonnull BankReference bankAccount)
    {
        this.editData(data, d -> d.setBankAccount(PlayerReference.of(player),bankAccount));
    }

    @Nonnull
    public List<ItemStack> getOverflowItems(@Nonnull CoinChestUpgradeData data) { return this.getData(data).getOverflowItems(); }
    public void clearOverflowItems(@Nonnull CoinChestMenu menu, @Nonnull CoinChestUpgradeData data)
    {
        BankUpgradeData d = this.getData(data);
        for(ItemStack item : d.getOverflowItems())
            ItemHandlerHelper.giveItemToPlayer(menu.player,item);
        data.setData(ModDataComponents.BANK_UPGRADE_DATA,d.setOverflowItems(new ArrayList<>()));
    }

    @Override
    public int getTickFrequency() { return 100; }

    @Override
    public void OnServerTick(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) {
        BankUpgradeData d = data.getData(ModDataComponents.BANK_UPGRADE_DATA,BankUpgradeData.DEFAULT);
        if(d.canInteract())
            this.TryInteract(be,data);
    }

    private void TryInteract(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) {
        BankUpgradeData d = data.getData(ModDataComponents.BANK_UPGRADE_DATA,BankUpgradeData.DEFAULT);
        if(d.targetAccount != null && d.player != null && d.getOverflowItems().isEmpty())
        {
            BankReference br = d.targetAccount.flagAsClient(be);
            if(br.allowedAccess(d.player))
            {
                IBankAccount account = br.get();
                if(account == null)
                    return;
                List<ItemStack> overflowItems = new ArrayList<>();
                IMoneyHandler handler = MoneyAPI.API.GetContainersMoneyHandler(be.getStorage(),overflowItems::add,be);
                MoneyView contents = handler.getStoredMoney();
                if(d.depositMode && contents.containsValue(d.moneyLimit))
                {
                    //Deposit all money within the container
                    for(MoneyValue value : contents.allValues())
                    {
                        if(value.sameType(d.moneyLimit))
                        {
                            if(handler.extractMoney(value,true).getCoreValue() < value.getCoreValue())
                            {
                                //Some money was able to be extracted, so we'll deposit what we can
                                MoneyValue result = handler.extractMoney(value,false);
                                MoneyValue takenAmount = value.subtractValue(result);
                                if(!takenAmount.isEmpty())
                                    account.depositMoney(takenAmount);
                            }
                        }
                    }
                }
                if(!d.depositMode && !contents.containsValue(d.moneyLimit))
                {
                    //Withdraw money until we reach the target goal
                    MoneyValue available = contents.valueOf(d.moneyLimit.getUniqueName());
                    MoneyValue targetToTake = d.moneyLimit.subtractValue(available);
                    if(!targetToTake.isEmpty())
                    {
                        MoneyValue taken = account.withdrawMoney(targetToTake);
                        MoneyValue result = handler.insertMoney(taken,false);
                        if(!result.isEmpty())
                        {
                            //If the container couldn't accept all the money, return the remainder to the bank account
                            account.depositMoney(result);
                        }
                    }
                }
                if(!overflowItems.isEmpty())
                    d.setOverflowItems(overflowItems);
            }
        }
    }

    @Override
    public void addClientTabs(@Nonnull CoinChestUpgradeData data, @Nonnull Object screen, @Nonnull Consumer<Object> consumer) {
        consumer.accept(new BankUpgradeSelectTab(data,screen));
        consumer.accept(new BankUpgradeSettingsTab(data,screen));
    }

    @Nonnull
    @Override
    public List<Component> getTooltip(@Nonnull UpgradeData data) { return Lists.newArrayList(LCText.TOOLTIP_UPGRADE_BANK.get()); }

    @Override
    public boolean clearDataFromStack(@Nonnull ItemStack stack) { return this.clearData(stack,ModDataComponents.BANK_UPGRADE_DATA); }

}
