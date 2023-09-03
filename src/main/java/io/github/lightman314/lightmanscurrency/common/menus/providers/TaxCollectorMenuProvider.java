package io.github.lightman314.lightmanscurrency.common.menus.providers;

import io.github.lightman314.lightmanscurrency.common.menus.TaxCollectorMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class TaxCollectorMenuProvider implements EasyMenuProvider {

    private final long entryID;
    private final MenuValidator validator;
    public TaxCollectorMenuProvider(long entryID, MenuValidator validator) { this.entryID = entryID; this.validator = validator; }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player player) { return new TaxCollectorMenu(id, inventory, entryID, validator); }

}
