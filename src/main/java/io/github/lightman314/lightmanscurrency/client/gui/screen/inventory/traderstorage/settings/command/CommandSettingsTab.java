package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.command;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.traders.commands.CommandTrader;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommandSettingsTab extends SettingsSubTab {

    public CommandSettingsTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.REPEATING_COMMAND_BLOCK); }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_COMMAND.get(); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.EDIT_SETTINGS); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {
        this.addChild(PlainButton.builder()
                .sprite(IconAndButtonUtil.SPRITE_PLUS)
                .position(screenArea.pos.offset(20,30))
                .pressAction(() -> this.editPermissionLevel(1))
                .addon(EasyAddonHelper.activeCheck(() -> this.getPermissionLevel() < LCConfig.SERVER.commandTraderMaxPermissionLevel.get()))
                .build()
        );
        this.addChild(PlainButton.builder()
                .sprite(IconAndButtonUtil.SPRITE_MINUS)
                .position(screenArea.pos.offset(20,40))
                .pressAction(() -> this.editPermissionLevel(-1))
                .addon(EasyAddonHelper.activeCheck(() -> this.getPermissionLevel() > 0))
                .build()
        );
    }

    private int getPermissionLevel() {
        if(this.menu.getTrader() instanceof CommandTrader trader)
            return trader.getPermissionLevel();
        return 0;
    }

    private void editPermissionLevel(int delta)
    {
        this.sendMessage(this.builder().setInt("ChangePermissionLevel",this.getPermissionLevel() + delta));
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        gui.drawString(LCText.GUI_TRADER_SETTINGS_COMMAND_PERMISSION_LEVEL.get(this.getPermissionLevel()), 34, 35, 0x404040);

    }

}