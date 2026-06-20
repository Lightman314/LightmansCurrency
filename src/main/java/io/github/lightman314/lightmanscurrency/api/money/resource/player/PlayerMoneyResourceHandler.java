package io.github.lightman314.lightmanscurrency.api.money.resource.player;

import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import net.minecraft.world.entity.player.Player;

public interface PlayerMoneyResourceHandler extends MoneyResourceHandler {

    void updatePlayer(Player player);

}
