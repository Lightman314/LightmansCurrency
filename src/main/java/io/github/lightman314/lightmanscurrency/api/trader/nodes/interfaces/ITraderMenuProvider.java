package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import javax.annotation.Nullable;

public interface ITraderMenuProvider {

    @Nullable
    default MenuProvider customerMenuProvider(Player player, MenuValidator validator) { return null; }
    @Nullable
    default MenuProvider storageMenuProvider(Player player, MenuValidator validator) { return null; }

}