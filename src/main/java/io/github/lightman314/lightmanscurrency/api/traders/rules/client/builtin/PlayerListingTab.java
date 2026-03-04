package io.github.lightman314.lightmanscurrency.api.traders.rules.client.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.traders.rules.client.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.rules.client.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerAction;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerListWidget;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.rules.builtin.PlayerListing;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PlayerListingTab extends TradeRuleSubTab<PlayerListing> {

    public PlayerListingTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent,PlayerListing.TYPE); }

    EasyButton buttonToggleMode;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.buttonToggleMode = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,10))
                .width(screenArea.width - 40)
                .text(this::getModeText)
                .pressAction(this::PressToggleModeButton)
                .build());

        this.addChild(PlayerListWidget.builder()
                .position(screenArea.pos.offset(20,34))
                .width(screenArea.width - 40)
                .rows(3)
                .addPlayer(this::AddPlayer)
                .action(PlayerAction.easyRemove(this::RemovePlayer).build())
                .playerList(this::getPlayers)
                .build());

    }

    protected boolean isWhitelistMode() {
        PlayerListing rule = this.getRule();
        return rule == null || rule.isWhitelistMode();
    }

    protected Component getModeText()
    {
        return this.isWhitelistMode() ? LCText.BUTTON_PLAYER_LISTING_MODE_WHITELIST.get() : LCText.BUTTON_PLAYER_LISTING_MODE_BLACKLIST.get();
    }

    private List<PlayerReference> getPlayers()
    {
        PlayerListing rule = this.getRule();
        if(rule == null)
            return new ArrayList<>();
        return rule.getPlayerList();
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    void AddPlayer(PlayerReference player)
    {
        this.sendUpdateMessage(this.builder().setTag("AddPlayer",player.save()));
    }

    void RemovePlayer(PlayerReference player)
    {
        this.sendUpdateMessage(this.builder().setTag("RemovePlayer",player.save()));
    }

    void PressToggleModeButton(EasyButton button)
    {
        PlayerListing rule = this.getRule();
        if(rule == null)
            return;
        this.sendUpdateMessage(this.builder().setBoolean("ChangeMode", rule.isBlacklistMode()));
    }

}
