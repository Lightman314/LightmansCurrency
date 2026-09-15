package io.github.lightman314.lightmanscurrency.api.coins.money;

import io.github.lightman314.lightmanscurrency.api.money.resource.player.PlayerMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.DeferredMoneyResourceHandler;

import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletAttachment;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletStorage;
import net.minecraft.world.entity.player.Player;

public class PlayerWalletHandler implements PlayerMoneyResourceHandler, DeferredMoneyResourceHandler {

    private Player player;
    private final boolean allowOverflow;
    public PlayerWalletHandler(Player player,boolean allowOverflow) { this.player = player; this.allowOverflow = allowOverflow; }

    @Override
    public void updatePlayer(Player player) { this.player = player; }

    @Override
    public MoneyResourceHandler getMoneyResourceHandler() {
        //Get players wallet attachment
        WalletAttachment attachment = this.player.getData(LCDataAttachments.WALLET);
        //Get the wallets storage using the attachment as the ItemAccess
        WalletStorage storage = new WalletStorage(attachment);
        //Wrap the inventory with a money handler and call it a day :)
        return storage.getMoneyResourceHandler(this.player);
    }

    @Override
    public String toString() { return "PlayerWalletHandler"; }

}