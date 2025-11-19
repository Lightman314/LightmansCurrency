package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerAction;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerListWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.IntParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerDiscounts;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PlayerDiscountTab extends TradeRuleSubTab<PlayerDiscounts> {


    public PlayerDiscountTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, PlayerDiscounts.TYPE); }

    TextBoxWrapper<Integer> discountInput;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        PlayerDiscounts rule = this.getRule();

        this.discountInput = this.addChild(TextInputUtil.intBuilder()
                .position(screenArea.pos.offset(20,9))
                .size(30,20)
                .parser(IntParser.ONE_TO_ONE_HUNDRED)
                .handler(this::onDiscountChanged)
                .maxLength(3)
                .startingValue(rule == null ? 1 : rule.getDiscount())
                .wrap().build());

        this.addChild(PlayerListWidget.builder()
                .position(screenArea.pos.offset(20,34))
                .width(screenArea.width - 40)
                .addPlayer(this::AddPlayer)
                .rows(3)
                .action(PlayerAction.easyRemove(this::RemovePlayer).build())
                .playerList(this::getPlayerList)
                .build());

    }

    private List<PlayerReference> getPlayerList()
    {
        PlayerDiscounts rule = this.getRule();
        if(rule == null)
            return new ArrayList<>();
        return rule.getPlayerList();
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        if(getRule() == null)
            return;

        gui.pushOffset(this.discountInput);
        gui.drawString(LCText.GUI_PLAYER_DISCOUNTS_INFO.get(), this.discountInput.getWidth() + 4, 6, 0x404040);
        gui.popOffset();

    }

    void AddPlayer(PlayerReference player)
    {
        this.sendUpdateMessage(this.builder().setCompound("AddPlayer",player.save()));
    }

    void RemovePlayer(PlayerReference player)
    {
        this.sendUpdateMessage(this.builder().setCompound("RemovePlayer",player.save()));
    }

    void onDiscountChanged(int discount)
    {
        PlayerDiscounts rule = this.getRule();
        if(rule != null)
            rule.setDiscount(discount);
        this.sendUpdateMessage(this.builder().setInt("Discount", discount));
    }

}