package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerAction;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerListWidget;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerListing;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PlayerListingTab extends TradeRuleSubTab<PlayerListing> {

    public PlayerListingTab(@Nonnull TradeRulesClientTab<?> parent, @Nonnull TradeRuleType<PlayerListing> ruleType) { super(parent, ruleType); }

    @Nonnull
    @Override
    public IconData getIcon() { return this.isWhitelistMode() ? IconUtil.ICON_WHITELIST : IconUtil.ICON_BLACKLIST; }

    EasyButton buttonToggleMode;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.buttonToggleMode = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,7))
                .width(screenArea.width - 40)
                .text(this::getModeText)
                .pressAction(this::PressToggleModeButton)
                .build());

        this.addChild(PlayerListWidget.builder()
                .position(screenArea.pos.offset(20,29))
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
        this.sendUpdateMessage(this.builder().setCompound("AddPlayer",player.save()));
    }

    void RemovePlayer(PlayerReference player)
    {
        this.sendUpdateMessage(this.builder().setCompound("RemovePlayer",player.save()));
    }

    void PressToggleModeButton(EasyButton button)
    {
        PlayerListing rule = this.getRule();
        if(rule == null)
            return;
        this.sendUpdateMessage(this.builder().setBoolean("ChangeMode", rule.isBlacklistMode()));
    }

}
