package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.MoneyResourceUtil;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.MoneyItemStorage;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.EasyResourceSlot;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.IEasySlot;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MoneyStorageTab extends TraderStorageTab {

    public static final Identifier KEY = LCApi.id("money_storage");

    public static final TextEntry TOOLTIP_MONEY_STORAGE = TextEntry.tooltip(LCApi.MODID,"trader.storage.money_storage");
    public static final TextEntry GUI_TRADER_MONEY_STORAGE_CONTENTS = TextEntry.gui(LCApi.MODID,"trader.money_storage.contents");
    public static final TextEntry BUTTON_TRADER_STORE_MONEY = TextEntry.button(LCApi.MODID,"trader.money_storage.store");
    public static final TextEntry BUTTON_TRADER_COLLECT_MONEY = TextEntry.button(LCApi.MODID,"trader.money_storage.collect");

    private final MoneyItemStorage moneyStorage = new MoneyItemStorage(9);
    public MoneyResourceHandler getMoneyResources() { return this.moneyStorage.getMoneyResourceHandler(this.getPlayer()); }
    private final List<Slot> slots = new ArrayList<>();
    public List<Slot> getSlots() { return this.slots; }

    public MoneyStorageTab(TraderStorageMenu menu) { super(menu); }

    @Override
    public Identifier getKey() { return KEY; }
    @Override
    public boolean canOpen() { return this.canStoreMoney() || this.canCollectMoney(); }
    @Override
    public int getTabSortPriority() { return -50; }

    @Override
    public void addMenuSlots(Consumer<Slot> builder) {
        //Add Money Slots
        for(int x = 0; x < this.moneyStorage.size(); ++x) {
            EasyResourceSlot slot = new EasyResourceSlot(this.moneyStorage,x,TraderStorageMenu.SLOT_OFFSET + 8 + x * 18,122);
            slot.setBackground(LCApi.id("container/slot/money"));
            this.slots.add(slot);
            builder.accept(slot);
            slot.setActive(false);
        }
    }

    @Override
    public void onTabOpened(FancyPacketMap additional) { IEasySlot.setActive(this.slots,true); }
    @Override
    public void onTabClosed() { IEasySlot.setActive(this.slots,false); }
    @Override
    public void onMenuClosed() { this.getMenu().clearContainer(this.moneyStorage); }

    public boolean canStoreMoney() {
        return this.getPermission(BuiltInPermissions.STORE_MONEY);
    }

    public boolean canCollectMoney() {
        return this.getPermission(BuiltInPermissions.COLLECT_MONEY);
    }

    public void storeMoney(MoneyValue amount) {
        MoneyStorageNode node = this.getNode(MoneyStorageNode.TYPE);
        if(node == null || !this.canStoreMoney())
            return;
        if(this.isClient()) {
            this.send(FancyPacketMap.map().set("storeMoney",LCFancyPacketTypes.MONEY,amount));
            return;
        }
        try(Transaction transaction = Transaction.openRoot()) {
            MoneyResourceHandler slotMoney = this.getMoneyResources();
            MoneyResourceHandler playerMoney = LCApi.getMoneyAPI().getPlayersMoneyHandler(this.getPlayer());
            MoneyResourceHandler traderStorage = node.getStorage();
            if(amount.isEmpty()) {
                for(MoneyValue value : slotMoney.getAllResources())
                    MoneyResourceUtil.move(slotMoney,traderStorage,value,transaction);
                transaction.commit();
            }
            else {
                //Only attempt to insert the given amount
                MoneyValue extracted = slotMoney.extract(amount,transaction);
                //If not enough was in the slots, take from the player directly
                if(extracted.getInternalValue() < amount.getInternalValue())
                    extracted = extracted.addValue(playerMoney.extract(amount.subtractValue(extracted),transaction));
                //Now insert into the trader
                if(extracted != null && !extracted.isEmpty()) {
                    MoneyValue inserted = traderStorage.insert(extracted,transaction);
                    if(inserted.equals(extracted))
                        transaction.commit();
                }
            }
        }
    }

    public void collectMoney(MoneyValue amount) {
        MoneyStorageNode node = this.getNode(MoneyStorageNode.TYPE);
        if(node == null || !this.canCollectMoney())
            return;
        if(this.isClient()) {
            this.send(FancyPacketMap.map().set("collectMoney",LCFancyPacketTypes.MONEY,amount));
            return;
        }
        try(Transaction transaction = Transaction.openRoot()) {
            MoneyResourceHandler playerMoney = LCApi.getMoneyAPI().getPlayersMoneyHandler(this.getPlayer());
            MoneyResourceHandler traderMoney = node.getStorage();
            if(amount.isEmpty()) {
                //Take all money from the traders money storage and give it to the player
                for(MoneyValue value : traderMoney.getAllResources())
                    MoneyResourceUtil.move(traderMoney,playerMoney,value,transaction);

            }
            else {
                //Take the money from the traders storage
                MoneyResourceUtil.move(traderMoney,playerMoney,amount,transaction);
            }
            transaction.commit();
        }
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("storeMoney"))
            this.storeMoney(message.get("storeMoney",LCFancyPacketTypes.MONEY));
        if(message.contains("collectMoney"))
            this.collectMoney(message.get("collectMoney",LCFancyPacketTypes.MONEY));
    }
}