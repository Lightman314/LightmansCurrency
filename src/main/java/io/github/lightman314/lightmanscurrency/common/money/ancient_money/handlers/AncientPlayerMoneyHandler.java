package io.github.lightman314.lightmanscurrency.common.money.ancient_money.handlers;

import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.capability.money.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.items.data.WalletDataWrapper;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.AncientMoneyType;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.AncientMoneyValue;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class AncientPlayerMoneyHandler extends MoneyHandler implements IPlayerMoneyHandler {


    private Player player;
    private WalletHandler walletHandler;

    public AncientPlayerMoneyHandler(@Nonnull Player player) { this.updatePlayer(player); }

    @Nonnull
    @Override
    public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) {
        if(this.walletHandler != null && insertAmount instanceof AncientMoneyValue amount)
        {
            ItemStack wallet = this.walletHandler.getWallet();
            if(wallet.isEmpty() || !WalletItem.isWallet(wallet))
                return insertAmount;
            WalletDataWrapper data = WalletItem.getDataWrapper(wallet);
            Container contents = data.getContents();
            IMoneyHandler handler = AncientMoneyType.INSTANCE.createMoneyHandlerForContainer(contents, i -> ItemHandlerHelper.giveItemToPlayer(this.player,i),IClientTracker.entityWrapper(this.player));
            MoneyValue result = handler.insertMoney(insertAmount,simulation);
            data.setContents(contents,this.player);
            return result;
        }
        return insertAmount;
    }

    @Nonnull
    @Override
    public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation) {
        if(this.walletHandler != null && extractAmount instanceof AncientMoneyValue amount)
        {
            ItemStack wallet = this.walletHandler.getWallet();
            if(wallet.isEmpty() || !WalletItem.isWallet(wallet))
                return extractAmount;
            WalletDataWrapper data = WalletItem.getDataWrapper(wallet);
            Container contents = data.getContents();
            IMoneyHandler handler = AncientMoneyType.INSTANCE.createMoneyHandlerForContainer(contents,i -> ItemHandlerHelper.giveItemToPlayer(this.player,i),IClientTracker.entityWrapper(this.player));
            MoneyValue result = handler.extractMoney(extractAmount,simulation);
            data.setContents(contents,this.player);
            return result;
        }
        return extractAmount;
    }

    @Override
    public boolean isMoneyTypeValid(@Nonnull MoneyValue value) { return value instanceof AncientMoneyValue; }

    @Override
    public void updatePlayer(@Nonnull Player player) {
        this.player = player;
        this.walletHandler = WalletHandler.get(player);
    }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        if(this.walletHandler != null)
        {
            ItemStack wallet = this.walletHandler.getWallet();
            if(wallet.isEmpty() || !WalletItem.isWallet(wallet))
                return;
            WalletDataWrapper data = WalletItem.getDataWrapper(wallet);
            Container contents = data.getContents();
            IMoneyHandler handler = AncientMoneyType.INSTANCE.createMoneyHandlerForContainer(contents,i -> ItemHandlerHelper.giveItemToPlayer(this.player,i),IClientTracker.entityWrapper(this.player));
            builder.merge(handler.getStoredMoney());
        }
    }

}
