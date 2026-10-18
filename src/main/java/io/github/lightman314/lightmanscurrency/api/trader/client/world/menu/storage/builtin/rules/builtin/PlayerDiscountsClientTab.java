package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.player.PlayerAction;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.player.PlayerListWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.IntParser;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.RuleClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.rules.builtin.PlayerDiscounts;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;

public class PlayerDiscountsClientTab extends RuleClientTab<PlayerDiscounts> {

    public PlayerDiscountsClientTab(TradeRulesClientTab tab) { super(tab); }

    private TextBoxWrapper<Integer> discountInput;

    @Override
    public TradeRuleType<PlayerDiscounts> getType() { return PlayerDiscounts.TYPE; }

    @Override
    protected void initialize(ScreenArea area) {

        this.discountInput = this.addChild(TextBoxWrapper.intBuilder()
                .atPos(area.pos.offset(20,9))
                .ofWidth(30)
                .parser(IntParser.ONE_TO_ONE_HUNDRED)
                .withHandler(this::onDiscountChanged)
                .withMaxLength(3)
                .withStartingValue(this.getRule().getDiscount())
                .withOldWidget(this.discountInput)
                .build());

        this.addChild(PlayerListWidget.builder()
                .atPos(area.pos.offset(20,34))
                .ofWidth(area.width - 40)
                .addPlayer(this::addPlayer)
                .withRows(3)
                .action(PlayerAction.easyRemove(this::removePlayer).build())
                .playerList(() -> this.getRule().getPlayerList())
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        gui.push(this.discountInput.getPosition());
        gui.text(PlayerDiscounts.GUI_LABEL.get(),this.discountInput.getWidth() + 4,6,0xFF404040,false);
        gui.pop();
    }

    private void addPlayer(PlayerReference player) {
        this.requestChange(FancyPacketMap.map().set("addPlayer",LCFancyPacketTypes.PLAYER_REFERENCE,player));
    }

    private void removePlayer(PlayerReference player) {
        this.requestChange(FancyPacketMap.map().set("removePlayer",LCFancyPacketTypes.PLAYER_REFERENCE,player));
    }

    private void onDiscountChanged(int discount) {
        this.requestChange(FancyPacketMap.map().setInt("setDiscount",discount));
    }

}
