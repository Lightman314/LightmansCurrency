package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerWhitelist;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public class PlayerWhitelistTab extends TradeRuleSubTab<PlayerWhitelist> {

    public PlayerWhitelistTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, PlayerWhitelist.TYPE); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_WHITELIST; }

    EditBox nameInput;

    EasyButton buttonAddPlayer;
    EasyButton buttonRemovePlayer;

    ScrollTextDisplay playerDisplay;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.nameInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 10, screenArea.y + 9, screenArea.width - 20, 20, EasyText.empty()));

        this.buttonAddPlayer = this.addChild(new EasyTextButton(screenArea.pos.offset(10, 30), 78, 20, EasyText.translatable("gui.button.lightmanscurrency.whitelist.add"), this::PressWhitelistButton));
        this.buttonRemovePlayer = this.addChild(new EasyTextButton(screenArea.pos.offset(screenArea.width - 88, 30), 78, 20, EasyText.translatable("gui.button.lightmanscurrency.whitelist.remove"), this::PressForgetButton));

        //Player list display
        this.playerDisplay = this.addChild(new ScrollTextDisplay(screenArea.pos.offset(7, 55), screenArea.width - 14, 84, this::getWhitelistedPlayers));
        this.playerDisplay.setColumnCount(2);

    }

    private List<Component> getWhitelistedPlayers()
    {
        List<Component> playerList = Lists.newArrayList();
        PlayerWhitelist rule = this.getRule();
        if(rule == null)
            return playerList;
        for(PlayerReference player : rule.getWhitelistedPlayers())
            playerList.add(player.getNameComponent(true));
        return playerList;
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    void PressWhitelistButton(EasyButton button)
    {
        String name = nameInput.getValue();
        if(!name.isBlank())
        {
            nameInput.setValue("");
            this.sendUpdateMessage(LazyPacketData.builder()
                    .setBoolean("Add", true)
                    .setString("Name", name));
        }
    }

    void PressForgetButton(EasyButton button)
    {
        String name = nameInput.getValue();
        if(!name.isBlank())
        {
            nameInput.setValue("");
            this.sendUpdateMessage(LazyPacketData.builder()
                    .setBoolean("Add", false)
                    .setString("Name", name));
        }

    }

}
