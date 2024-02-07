package io.github.lightman314.lightmanscurrency.common.menus.providers;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.TerminalMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class TerminalMenuProvider implements MenuProvider {

    private final MenuValidator validator;
    public TerminalMenuProvider(@Nonnull MenuValidator validator) { this.validator = validator; }

    @Nonnull
    @Override
    public Component getDisplayName() { return EasyText.translatable("block.lightmanscurrency.terminal"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player player) { return new TerminalMenu(id, inventory, this.validator); }

    public static void OpenMenu(@Nonnull ServerPlayer player, @Nonnull MenuValidator validator)
    {
        NetworkHooks.openScreen(player, new TerminalMenuProvider(validator), EasyMenu.encoder(validator));
    }

}
