package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TerminalMenu extends EasyMenu implements IValidatedMenu {

    private final MenuValidator validator;

    @Override
    public MenuValidator getValidator() { return this.validator; }

    public TerminalMenu(int id, Inventory inventory, MenuValidator validator)
    {
        super(ModMenus.NETWORK_TERMINAL.get(), id, inventory, validator);
        this.validator = validator;
        //Flag the validator has having gone through the network
        this.validator.isThroughNetwork = true;
        this.addValidator(() -> !QuarantineAPI.IsDimensionQuarantined(this));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

}
