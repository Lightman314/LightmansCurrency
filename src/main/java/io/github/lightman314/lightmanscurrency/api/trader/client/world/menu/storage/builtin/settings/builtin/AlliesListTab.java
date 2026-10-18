package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.player.PlayerAction;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.player.PlayerListWidget;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.AlliesNode;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.List;

public class AlliesListTab extends SettingsSubTab.ForNode<AlliesNode> {

    public AlliesListTab(SettingsClientTab parent) { super(parent,AlliesNode.TYPE); }

    @Override
    public IconData getIcon() { return ItemIcon.of(Items.PLAYER_HEAD); }
    @Override
    public Component getName() { return AlliesNode.VALUE_ALLIES.get(); }

    @Override
    public boolean isVisible() { return this.getPermission(LCPermissions.ADD_REMOVE_ALLIES); }
    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {

        this.addChild(PlayerListWidget.builder()
                .atPos(area.pos.offset(20,16))
                .ofWidth(area.width - 40)
                .withRows(4)
                .action(PlayerAction.easyRemove(this::removeAlly).build())
                .addPlayer(this::addAlly)
                .playerList(this::getAllyList)
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) { }

    private List<PlayerReference> getAllyList() { return this.getNodeValue(AlliesNode::getAllies,List.of()); }

    private void addAlly(PlayerReference player) {
        this.sendSettingRequest(FancyPacketMap.map().set("addAlly",LCFancyPacketTypes.PLAYER_REFERENCE,player));
    }

    private void removeAlly(PlayerReference player) {
        this.sendSettingRequest(FancyPacketMap.map().set("removeAlly",LCFancyPacketTypes.PLAYER_REFERENCE,player));
    }

}