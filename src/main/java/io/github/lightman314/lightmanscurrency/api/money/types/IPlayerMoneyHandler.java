package io.github.lightman314.lightmanscurrency.api.money.types;

import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHandler;
import net.minecraft.world.entity.player.Player;

public interface IPlayerMoneyHandler extends IMoneyHandler {

    void updatePlayer(Player player);

}
