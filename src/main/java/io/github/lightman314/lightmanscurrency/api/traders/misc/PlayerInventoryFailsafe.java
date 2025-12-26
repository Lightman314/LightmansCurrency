package io.github.lightman314.lightmanscurrency.api.traders.misc;

import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.IItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class PlayerInventoryFailsafe implements IMoneyHandler {

    private final Player player;
    public PlayerInventoryFailsafe(Player player) { this.player = player; }

    @Nonnull
    @Override
    public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) {
        if(insertAmount instanceof IItemBasedValue value)
        {
            //Give all money items to the player
            if(!simulation)
            {
                for(ItemStack stack : value.getAsSeperatedItemList())
                    ItemHandlerHelper.giveItemToPlayer(this.player,stack);
            }
            return MoneyValue.empty();
        }
        return insertAmount;
    }

    @Nonnull
    @Override
    public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation) { return extractAmount; }

    @Override
    public boolean isMoneyTypeValid(@Nonnull MoneyValue value) { return value instanceof IItemBasedValue; }

    @Nonnull
    @Override
    public MoneyView getStoredMoney() { return MoneyView.empty(); }
}
