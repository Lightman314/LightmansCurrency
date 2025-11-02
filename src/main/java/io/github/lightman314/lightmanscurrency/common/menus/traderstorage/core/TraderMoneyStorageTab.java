package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core;

import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.menus.MoneySlot;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.core.TraderMoneyStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TraderMoneyStorageTab extends TraderStorageTab {

    public TraderMoneyStorageTab(ITraderStorageMenu menu) { super(menu); }

    private final Container coinSlotContainer = new SimpleContainer(5);
    private final List<MoneySlot> coinSlots = new ArrayList<>();
    public List<MoneySlot> getCoinSlots() { return new ArrayList<>(this.coinSlots); }
    public IMoneyHandler getCoinSlotHandler() { return MoneyAPI.getApi().GetContainersMoneyHandler(this.coinSlotContainer,this.menu.getPlayer()); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(Object screen) { return new TraderMoneyStorageClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) { return this.canStoreMoney() || this.canCollectMoney(); }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
        //Add Coin Slots
        for(int x = 0; x < this.coinSlotContainer.getContainerSize(); ++x)
        {
            MoneySlot slot = new MoneySlot(this.coinSlotContainer,x, TraderStorageMenu.SLOT_OFFSET + 8 + (x + 4) * 18, 122, this.menu.getPlayer());
            this.coinSlots.add(slot);
            addSlot.apply(slot);
            slot.active = false;
        }
    }

    @Override
    public void onTabOpen() { EasySlot.SetActive(this.coinSlots); }
    @Override
    public void onTabClose() { EasySlot.SetInactive(this.coinSlots); }
    @Override
    public void onMenuClose() { this.menu.clearContainer(this.coinSlotContainer); }

    public boolean canStoreMoney()
    {
        TraderData trader = this.menu.getTrader();
        return trader != null && trader.canStoreMoney() && !trader.isLinkedToBank() && trader.hasPermission(this.menu.getPlayer(),Permissions.STORE_COINS);
    }
    public boolean canCollectMoney()
    {
        TraderData trader = this.menu.getTrader();
        return trader != null && trader.hasPermission(this.menu.getPlayer(),Permissions.COLLECT_COINS) && ((trader.canStoreMoney() && !trader.isLinkedToBank()) || !trader.getInternalStoredMoney().isEmpty());
    }

    public void storeMoney(final MoneyValue amount) {
        TraderData trader = this.menu.getTrader();
        if(trader != null && this.canStoreMoney())
        {
            //First attempt to store money from the coin slots
            IMoneyHandler coinSlotHandler = this.getCoinSlotHandler();
            MoneyView coinSlotContents = coinSlotHandler.getStoredMoney();
            if(amount.isEmpty())
            {
                //If amount is empty *only* store money from the coin slots
                for(MoneyValue value : coinSlotContents.allValues())
                {
                    if(coinSlotHandler.extractMoney(value,true).isEmpty())
                    {
                        //Put money from coin slots into storage
                        coinSlotHandler.extractMoney(value,false);
                        trader.addStoredMoney(value,false);
                    }
                }
                if(this.isClient())
                    this.menu.SendMessage(this.builder().setMoneyValue("StoreMoney",amount));
                return;
            }
            MoneyValue amountLeft = amount;
            //Check and see if the coin slots have any money of that type
            if(!coinSlotContents.valueOf(amount.getUniqueName()).isEmpty())
            {
                amountLeft = coinSlotHandler.extractMoney(amount,false);
                MoneyValue insertAmount = amount.subtractValue(amountLeft);
                trader.addStoredMoney(insertAmount,false);
            }
            //If the coin slots did not have sufficient funds, insert from the players money
            if(!amountLeft.isEmpty())
            {
                //Take from player directly
                IMoneyHandler playerHandler = MoneyAPI.getApi().GetPlayersMoneyHandler(this.menu.getPlayer());
                MoneyValue remainder = playerHandler.extractMoney(amountLeft,false);
                MoneyValue insertAmount = amountLeft.subtractValue(remainder);
                trader.addStoredMoney(insertAmount,false);
            }
            if(this.isClient())
                this.menu.SendMessage(this.builder().setMoneyValue("StoreMoney",amount));
        }
    }

    public void collectMoney(final MoneyValue amount) {
        TraderData trader = this.menu.getTrader();
        if(trader != null && this.canCollectMoney())
        {
            IMoneyHandler playerHandler = MoneyAPI.getApi().GetPlayersMoneyHandler(this.menu.getPlayer());
            MoneyStorage moneyStorage = trader.getInternalStoredMoney();
            if(amount.isEmpty())
            {
                //Take all money from the traders money storage and give it to the player
                for(MoneyValue value : trader.getInternalStoredMoney().allValues())
                {
                    MoneyValue remainder = playerHandler.insertMoney(value,false);
                    if(remainder.isEmpty())
                        moneyStorage.removeValue(value);
                    else
                        moneyStorage.removeValue(value.subtractValue(remainder));
                }
            }
            else
            {
                MoneyValue storedAmount = moneyStorage.valueOf(amount.getUniqueName());
                MoneyValue takeAmount = amount;
                if(amount.getCoreValue() > storedAmount.getCoreValue())
                    takeAmount = storedAmount;
                if(!takeAmount.isEmpty())
                {
                    MoneyValue remainder = playerHandler.insertMoney(takeAmount,false);
                    if(remainder.isEmpty())
                        moneyStorage.removeValue(takeAmount);
                    else
                        moneyStorage.removeValue(takeAmount.subtractValue(remainder));
                }
            }
            if(this.isClient())
                this.menu.SendMessage(this.builder().setMoneyValue("CollectMoney",amount));
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("StoreMoney"))
            this.storeMoney(message.getMoneyValue("StoreMoney"));
        if(message.contains("CollectMoney"))
            this.collectMoney(message.getMoneyValue("CollectMoney"));
    }

}