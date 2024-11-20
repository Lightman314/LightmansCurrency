package io.github.lightman314.lightmanscurrency.common.menus.providers;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.menus.TerminalMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import net.minecraft.ChatFormatting;
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
    public Component getDisplayName() { return ModBlocks.TERMINAL.get().getName(); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player player) { return new TerminalMenu(id, inventory, this.validator); }

    public static void OpenMenu(@Nonnull Player player, @Nonnull MenuValidator validator)
    {
        if(QuarantineAPI.IsDimensionQuarantined(player))
            EasyText.sendMessage(player, LCText.MESSAGE_DIMENSION_QUARANTINED_TERMINAL.getWithStyle(ChatFormatting.GOLD));
        else if(player instanceof ServerPlayer sp)
            NetworkHooks.openScreen(sp,new TerminalMenuProvider(validator), EasyMenu.encoder(validator));
    }

}