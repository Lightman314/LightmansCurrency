package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class TerminalMenu extends EasyMenu implements IValidatedMenu {

    private final MenuValidator validator;

    @Nonnull
    @Override
    public MenuValidator getValidator() { return this.validator; }

    public TerminalMenu(int id, Inventory inventory, MenuValidator validator)
    {
        super(ModMenus.NETWORK_TERMINAL.get(), id, inventory, validator);
        this.validator = validator;
        this.addValidator(() -> !QuarantineAPI.IsDimensionQuarantined(this));
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int slot) { return ItemStack.EMPTY; }

}
