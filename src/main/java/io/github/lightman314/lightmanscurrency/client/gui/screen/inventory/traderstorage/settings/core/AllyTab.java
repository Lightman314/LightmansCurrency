package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerAction;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerListWidget;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AllyTab extends SettingsSubTab {

    public AllyTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    @Override
    @Nonnull
    public IconData getIcon() { return IconData.of(Items.PLAYER_HEAD); }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_ALLY.get(); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.ADD_REMOVE_ALLIES); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.addChild(PlayerListWidget.builder()
                .position(screenArea.pos.offset(20,10))
                .width(screenArea.width - 40)
                .rows(4)
                .action(PlayerAction.easyRemove(this::RemoveAlly).build())
                .addPlayer(this::AddAlly)
                .playerList(this::getAllyList)
                .build());

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    private List<PlayerReference> getAllyList()
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return trader.getAllies();
        return new ArrayList<>();
    }

    private void AddAlly(@Nonnull PlayerReference player)
    {
        this.sendMessage(this.builder().setCompound("AddAlly",player.save()));
    }

    private void RemoveAlly(@Nonnull PlayerReference player)
    {
        this.sendMessage(this.builder().setCompound("RemoveAlly",player.save()));
    }

}
