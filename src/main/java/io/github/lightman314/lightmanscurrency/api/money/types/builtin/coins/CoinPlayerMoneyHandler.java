package io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins;

import io.github.lightman314.lightmanscurrency.api.capability.money.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class CoinPlayerMoneyHandler extends MoneyHandler implements IPlayerMoneyHandler {


    private Player player;
    private WalletHandler walletHandler;
    private ItemStack cachedWallet = ItemStack.EMPTY;

    public CoinPlayerMoneyHandler(@Nonnull Player player) { this.updatePlayer(player); }

    @Nonnull
    @Override
    public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) {
        if(this.walletHandler != null)
            return this.walletHandler.insertMoney(insertAmount, simulation);
        else if(insertAmount instanceof CoinValue coinValue)
        {
            //Manually give money to the players inventory
            if(!simulation)
            {
                for(ItemStack stack : coinValue.getAsSeperatedItemList())
                    ItemHandlerHelper.giveItemToPlayer(this.player, stack);
            }
            return MoneyValue.empty();
        }
        return insertAmount;
    }

    @Nonnull
    @Override
    public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation) {
        if(this.walletHandler != null)
            return this.walletHandler.extractMoney(extractAmount, simulation);
        return extractAmount;
    }

    @Override
    public boolean isMoneyTypeValid(@Nonnull MoneyValue value) { return value instanceof CoinValue; }

    @Override
    public void updatePlayer(@Nonnull Player player) {
        this.player = player;
        this.walletHandler = WalletHandler.get(player);
    }

    @Override
    protected boolean hasStoredMoneyChanged() {
        if(this.walletHandler == null)
            return !this.cachedWallet.isEmpty(); //If the wallet handler is null but the cache isn't empty, we need to update the cache to empty/null
        return !InventoryUtil.ItemsFullyMatch(this.cachedWallet, this.walletHandler.getWallet());
    }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        if(this.walletHandler != null)
        {
            builder.merge(this.walletHandler.getStoredMoney());
            this.cachedWallet = this.walletHandler.getWallet().copy();
        }
        else //No wallet handler -> empty wallet cache
            this.cachedWallet = ItemStack.EMPTY;
    }

}
