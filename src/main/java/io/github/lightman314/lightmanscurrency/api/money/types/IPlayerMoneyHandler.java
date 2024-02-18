package io.github.lightman314.lightmanscurrency.api.money.types;

import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public interface IPlayerMoneyHandler extends IMoneyHandler {

    void updatePlayer(@Nonnull Player player);
}
