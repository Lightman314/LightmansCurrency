package io.github.lightman314.lightmanscurrency.api.misc.item_handlers;

import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MoneyInventory extends LCItemStackHandler implements IMoneyHandler {

    private final Player player;
    private final IMoneyHandler handler;
    public MoneyInventory(Player player,int size) {
        super(size);
        this.player = player;
        this.handler = MoneyAPI.getApi().GetContainersMoneyHandler(this,this.player);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) { return MoneyAPI.getApi().ItemAllowedInMoneySlot(this.player,stack); }
    @Override
    public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) { return this.handler.insertMoney(insertAmount,simulation); }
    @Override
    public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation) { return this.handler.extractMoney(extractAmount,simulation); }
    @Override
    public boolean isMoneyTypeValid(MoneyValue value) { return this.handler.isMoneyTypeValid(value); }
    @Override
    public MoneyView getStoredMoney() { return this.handler.getStoredMoney(); }

}
