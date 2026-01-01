package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
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
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    public boolean isDepositMode(@Nonnull CoinChestUpgradeData data) {
        CompoundTag tag = data.getItemTag();
        if(tag.contains("DepositMode"))
            return tag.getBoolean("DepositMode");
        return true;
    }
    public void setDepositMode(@Nonnull CoinChestUpgradeData data, boolean depositMode) {
        CompoundTag tag = data.getItemTag();
        tag.putBoolean("DepositMode",depositMode);
        data.setItemTag(tag);
    }

    @Nonnull
    public MoneyValue getMoneyLimit(@Nonnull CoinChestUpgradeData data) {
        CompoundTag tag = data.getItemTag();
        if(tag.contains("MoneyLimit"))
            return MoneyValue.safeLoad(tag,"MoneyLimit");
        return MoneyValue.empty();
    }
    public void setMoneyLimit(@Nonnull CoinChestUpgradeData data, @Nonnull MoneyValue moneyLimit) {
        CompoundTag tag = data.getItemTag();
        tag.put("MoneyLimit",moneyLimit.save());
        data.setItemTag(tag);
    }

    @Nullable
    public BankReference getTargetAccount(CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) {
        CompoundTag tag = data.getItemTag();
        if(tag.contains("TargetAccount"))
        {
            BankReference br = BankReference.load(tag.getCompound("TargetAccount"));
            if(br != null)
                br.flagAsClient(be);
        }
        return null;
    }
    private PlayerReference getPlayerContext(@Nonnull CoinChestUpgradeData data) {
        CompoundTag tag = data.getItemTag();
        if(tag.contains("PlayerContext"))
            return PlayerReference.load(tag.getCompound("PlayerContext"));
        return null;
    }
    @Nullable
    public IBankAccount getSelectedBankAccount(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data)
    {
        BankReference br = this.getTargetAccount(be,data);
        PlayerReference player = this.getPlayerContext(data);
        if(br != null && player != null && br.allowedAccess(player))
            return br.get();
        return null;
    }

    public void setSelectedBankAccount(@Nonnull CoinChestUpgradeData data, @Nonnull Player player, @Nonnull BankReference bankAccount)
    {
        CompoundTag tag = data.getItemTag();
        tag.put("TargetAccount",bankAccount.save());
        tag.put("PlayerContext",PlayerReference.of(player).save());
        data.setItemTag(tag);
    }

    @Nonnull
    public List<ItemStack> getOverflowItems(@Nonnull CoinChestUpgradeData data) {
        CompoundTag tag = data.getItemTag();
        if(tag.contains("OverflowItems"))
            return InventoryUtil.loadItemList("OverflowItems",tag);
        return new ArrayList<>();
    }
    private void setOverflowItems(@Nonnull CoinChestUpgradeData data, @Nonnull List<ItemStack> overflowItems) {
        CompoundTag tag = data.getItemTag();
        if(overflowItems.isEmpty())
            tag.remove("OverflowItems");
        else
            InventoryUtil.saveItemList("OverflowItems",tag,overflowItems);
        data.setItemTag(tag);
    }
    public void clearOverflowItems(@Nonnull CoinChestMenu menu, @Nonnull CoinChestUpgradeData data)
    {
        for(ItemStack item : this.getOverflowItems(data))
            ItemHandlerHelper.giveItemToPlayer(menu.player,item);
        this.setOverflowItems(data,new ArrayList<>());
    }

    @Override
    public int getTickFrequency() { return 100; }

    private boolean canInteract(CoinChestBlockEntity be, CoinChestUpgradeData data) {
        BankReference targetAccount = this.getTargetAccount(be,data);
        PlayerReference player = this.getPlayerContext(data);
        List<ItemStack> overflowItems = this.getOverflowItems(data);
        return targetAccount != null && player != null && overflowItems.isEmpty();
    }

    @Override
    public void OnServerTick(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) {
        if(QuarantineAPI.IsDimensionQuarantined(be))
            return;
        if(this.canInteract(be,data))
            this.TryInteract(be,data);
    }

    private void TryInteract(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) {
        BankReference targetAccount = this.getTargetAccount(be,data);
        PlayerReference player = this.getPlayerContext(data);
        List<ItemStack> overflowItems = this.getOverflowItems(data);

        if(targetAccount != null && player != null && overflowItems.isEmpty())
        {
            BankReference br = targetAccount.flagAsClient(be);
            if(br.allowedAccess(player))
            {

                IBankAccount account = br.get();
                if(account == null)
                    return;

                boolean depositMode = this.isDepositMode(data);
                MoneyValue moneyLimit = this.getMoneyLimit(data);

                overflowItems = new ArrayList<>();
                IMoneyHandler handler = MoneyAPI.getApi().GetContainersMoneyHandler(be.getStorage(),overflowItems::add,be);
                MoneyView contents = handler.getStoredMoney();
                if(depositMode && contents.containsValue(moneyLimit))
                {
                    //Deposit all money within the container
                    for(MoneyValue value : contents.allValues())
                    {
                        if(value.sameType(moneyLimit))
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
                if(!depositMode && !contents.containsValue(moneyLimit))
                {
                    //Withdraw money until we reach the target goal
                    MoneyValue available = contents.valueOf(moneyLimit.getUniqueName());
                    MoneyValue targetToTake = moneyLimit.subtractValue(available);
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
                    this.setOverflowItems(data,overflowItems);
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
    public boolean clearDataFromStack(@Nonnull ItemStack stack) { return this.clearData(stack,"DepositMode","MoneyLimit","TargetAccount","PlayerContext","OverflowItems"); }

}