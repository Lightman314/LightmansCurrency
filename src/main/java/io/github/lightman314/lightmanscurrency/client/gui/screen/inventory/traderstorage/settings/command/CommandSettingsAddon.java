package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.command;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.addons.MiscTabAddon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.traders.commands.CommandTrader;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandSettingsAddon extends MiscTabAddon {

    private int yPos = 0;

    @Override
    public void onOpenBefore(@Nonnull SettingsSubTab tab, @Nonnull ScreenArea screenArea, boolean firstOpen, @Nonnull AtomicInteger nextYPosition) {

        this.yPos = nextYPosition.getAndAdd(25);
        tab.addChild(PlainButton.builder()
                .sprite(IconAndButtonUtil.SPRITE_PLUS)
                .position(screenArea.pos.offset(30,this.yPos))
                .pressAction(() -> this.editPermissionLevel(1))
                .addon(EasyAddonHelper.activeCheck(() -> this.getPermissionLevel() < LCConfig.SERVER.commandTraderMaxPermissionLevel.get()))
                .build()
        );
        tab.addChild(PlainButton.builder()
                .sprite(IconAndButtonUtil.SPRITE_MINUS)
                .position(screenArea.pos.offset(30,this.yPos + 10))
                .pressAction(() -> this.editPermissionLevel(-1))
                .addon(EasyAddonHelper.activeCheck(() -> this.getPermissionLevel() > 0))
                .build()
        );
    }

    @Override
    public void onOpenAfter(@Nonnull SettingsSubTab tab, @Nonnull ScreenArea screenArea, boolean firstOpen, @Nonnull AtomicInteger nextYPosition) {

    }

    private int getPermissionLevel() {
        if(this.getTab() != null && this.getTab().menu.getTrader() instanceof CommandTrader trader)
            return trader.getPermissionLevel();
        return 0;
    }

    private void editPermissionLevel(int delta)
    {
        if(this.getTab() == null)
            return;
        this.getTab().sendMessage(this.getTab().builder().setInt("ChangePermissionLevel",this.getPermissionLevel() + delta));
    }

    @Override
    public void renderBG(@Nonnull SettingsSubTab tab, @Nonnull EasyGuiGraphics gui) {

        gui.drawString(LCText.GUI_TRADER_SETTINGS_COMMAND_PERMISSION_LEVEL.get(this.getPermissionLevel()), 44, this.yPos + 5, 0x404040);
    }

    @Override
    public void renderAfterWidgets(@Nonnull SettingsSubTab tab, @Nonnull EasyGuiGraphics gui) {

    }

    @Override
    public void tick(@Nonnull SettingsSubTab tab) {

    }

    @Override
    public void onClose(@Nonnull SettingsSubTab tab) {

    }

}
