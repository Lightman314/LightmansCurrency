package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.NodeSettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.AlliesNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerAction;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerListWidget;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class AllyTab extends NodeSettingsSubTab<AlliesNode> {

    public AllyTab(TraderSettingsClientTab parent) { super(AlliesNode.TYPE,parent); }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(Items.PLAYER_HEAD); }

    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_ALLY.get(); }

    @Override
    public boolean canOpen(AlliesNode node) { return this.menu.hasPermission(Permissions.ADD_REMOVE_ALLIES); }

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
    public void renderBG(EasyGuiGraphics gui) { }

    private List<PlayerReference> getAllyList()
    {
        AlliesNode node = this.getNode();
        if(node != null)
            return node.getAllies();
        return new ArrayList<>();
    }

    private void AddAlly(PlayerReference player)
    {
        this.sendMessage(this.builder().setCustom("AddAlly",player,ModLazyPackets.PLAYER_REFERENCE));
    }

    private void RemoveAlly(PlayerReference player)
    {
        this.sendMessage(this.builder().setCustom("RemoveAlly",player,ModLazyPackets.PLAYER_REFERENCE));
    }

}
