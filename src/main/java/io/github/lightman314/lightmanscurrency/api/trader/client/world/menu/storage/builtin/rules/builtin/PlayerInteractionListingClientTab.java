package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.player.PlayerAction;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.player.PlayerListWidget;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.RuleClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.rules.builtin.PlayerInteractionListing;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.network.chat.Component;

public class PlayerInteractionListingClientTab extends RuleClientTab<PlayerInteractionListing> {

    public PlayerInteractionListingClientTab(TradeRulesClientTab tab) { super(tab); }

    @Override
    public TradeRuleType<PlayerInteractionListing> getType() { return PlayerInteractionListing.TYPE; }

    private PlayerListWidget playerList = null;

    @Override
    protected void initialize(ScreenArea area) {

        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(20,10))
                .ofWidth(area.width - 40)
                .withText(this::getModeText)
                .onPress(this::pressToggleModeButton)
                .build());

        this.playerList = this.addChild(PlayerListWidget.builder()
                .atPos(area.pos.offset(20,34))
                .ofWidth(area.width - 40)
                .withRows(3)
                .addPlayer(this::addPlayer)
                .action(PlayerAction.easyRemove(this::removePlayer).build())
                .playerList(() -> this.getRule().getPlayerList())
                .withOldWidget(this.playerList)
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) { }

    private boolean isWhitelistMode() { return this.getRule().isWhitelistMode(); }

    private Component getModeText() { return this.getRule().isWhitelistMode() ? PlayerInteractionListing.BUTTON_MODE_WHITELIST.get() : PlayerInteractionListing.BUTTON_MODE_BLACKLIST.get(); }

    private void addPlayer(PlayerReference player) {
        this.requestChange(FancyPacketMap.map()
                .set("addPlayer",LCFancyPacketTypes.PLAYER_REFERENCE,player));
    }

    private void removePlayer(PlayerReference player) {
        this.requestChange(FancyPacketMap.map()
                .set("removePlayer",LCFancyPacketTypes.PLAYER_REFERENCE,player));
    }

    private void pressToggleModeButton() {
        this.requestChange(FancyPacketMap.map()
                .setBoolean("changeMode",!this.isWhitelistMode()));
    }

}