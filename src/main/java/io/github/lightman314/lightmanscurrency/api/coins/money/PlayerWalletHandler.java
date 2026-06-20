package io.github.lightman314.lightmanscurrency.api.coins.money;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.money.resource.player.PlayerMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.DeferredMoneyResourceHandler;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerWalletHandler implements PlayerMoneyResourceHandler, DeferredMoneyResourceHandler {

    private Player player;
    private boolean allowOverflow;
    public PlayerWalletHandler(Player player,boolean allowOverflow) { this.player = player; }

    @Override
    public void updatePlayer(Player player) { this.player = player; }

    @Override
    public MoneyResourceHandler getMoneyResourceHandler() {
        //TODO implement
        //Step 1: Get players wallet
        ItemStack wallet = LCApi.getCoinAPI().getEquippedWallet(this.player);
        //Step 2: Get wallets inventory (if present)
        //Step 3: Create Container Wrapper around wallets inventory
        //Step 4: Wrap the container wrapper such that when the change is commited the wallet items contents are also updated
        //Step 5: Profit :)
        return null;
    }

}
