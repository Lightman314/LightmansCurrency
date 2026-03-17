package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.MoneyInventory;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.MoneySlot;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.builtin.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageNodeTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.TraderMoneyStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.BankNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.EasySlot;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TraderMoneyStorageTab extends TraderStorageNodeTab<MoneyStorageNode> {

    public static final ResourceLocation KEY = LightmansCurrency.id("money_storage");

    public TraderMoneyStorageTab(ITraderStorageMenu menu) {
        super(MoneyStorageNode.TYPE,menu);
        this.coinSlotContainer = new MoneyInventory(menu.getPlayer(),5);
    }

    @Override
    public ResourceLocation tabKey() { return KEY; }

    private final MoneyInventory coinSlotContainer;
    private final List<MoneySlot> coinSlots = new ArrayList<>();
    public List<MoneySlot> getCoinSlots() { return new ArrayList<>(this.coinSlots); }
    public IMoneyHandler getCoinSlotHandler() { return MoneyAPI.getApi().GetContainersMoneyHandler(this.coinSlotContainer,this.menu.getPlayer()); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(Object screen) { return new TraderMoneyStorageClientTab(screen,this); }

    @Override
    protected boolean canOpenTab(Player player) { return this.canStoreMoney() || this.canCollectMoney(); }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
        //Add Coin Slots
        for(int x = 0; x < this.coinSlotContainer.getSlots(); ++x)
        {
            MoneySlot slot = new MoneySlot(this.coinSlotContainer,x, TraderStorageMenu.SLOT_OFFSET + 8 + (x + 4) * 18, 122);
            this.coinSlots.add(slot);
            addSlot.apply(slot);
            slot.setActive(false);
        }
    }

    @Override
    public void onTabOpen() { EasySlot.SetActive(this.coinSlots,true); }
    @Override
    public void onTabClose() { EasySlot.SetActive(this.coinSlots,false); }
    @Override
    public void onMenuClose() { this.menu.clearContainer(this.coinSlotContainer); }

    public boolean canStoreMoney()
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return false;
        MoneyStorageNode node = this.getNode();
        BankNode bankNode = trader.getNode(BankNode.TYPE);
        return node != null && trader.shouldStoreMoney() && (bankNode == null || !bankNode.isLinkedToBank()) && trader.hasPermission(this.menu.getPlayer(),Permissions.STORE_COINS);
    }
    public boolean canCollectMoney()
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return false;
        MoneyStorageNode node = this.getNode();
        BankNode bankNode = trader.getNode(BankNode.TYPE);
        return node != null && trader.hasPermission(this.menu.getPlayer(),Permissions.COLLECT_COINS) && ((trader.shouldStoreMoney() && (bankNode == null || !bankNode.isLinkedToBank())) || !node.getStorage().isEmpty());
    }

    public void storeMoney(final MoneyValue amount) {
        MoneyStorageNode node = this.getNode();
        if(node != null && this.canStoreMoney())
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
                        node.addStoredMoney(value,null);
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
                node.addStoredMoney(insertAmount,null);
            }
            //If the coin slots did not have sufficient funds, insert from the players money
            if(!amountLeft.isEmpty())
            {
                //Take from player directly
                IMoneyHandler playerHandler = MoneyAPI.getApi().GetPlayersMoneyHandler(this.menu.getPlayer());
                MoneyValue remainder = playerHandler.extractMoney(amountLeft,false);
                MoneyValue insertAmount = amountLeft.subtractValue(remainder);
                node.addStoredMoney(insertAmount,null);
            }
            if(this.isClient())
                this.menu.SendMessage(this.builder().setMoneyValue("StoreMoney",amount));
        }
    }

    public void collectMoney(final MoneyValue amount) {
        MoneyStorageNode node = this.getNode();
        if(node != null && this.canCollectMoney())
        {
            IMoneyHandler playerHandler = MoneyAPI.getApi().GetPlayersMoneyHandler(this.menu.getPlayer());
            MoneyStorage moneyStorage = node.getStorage();
            if(amount.isEmpty())
            {
                //Take all money from the traders money storage and give it to the player
                for(MoneyValue value : moneyStorage.allValues())
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
