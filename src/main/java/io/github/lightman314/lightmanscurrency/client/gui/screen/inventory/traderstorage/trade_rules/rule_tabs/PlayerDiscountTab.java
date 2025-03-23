package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerAction;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerListWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerDiscounts;
import net.minecraft.client.gui.components.EditBox;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PlayerDiscountTab extends TradeRuleSubTab<PlayerDiscounts> {


    public PlayerDiscountTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, PlayerDiscounts.TYPE); }

    EditBox discountInput;

    EasyButton buttonSetDiscount;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.discountInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 9, 20, 20, EasyText.empty()));
        this.discountInput.setMaxLength(2);
        PlayerDiscounts rule = this.getRule();
        if(rule != null)
            this.discountInput.setValue(Integer.toString(rule.getDiscount()));
        this.buttonSetDiscount = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(126,10))
                .width(60)
                .text(LCText.BUTTON_SET)
                .pressAction(this::PressSetDiscountButton)
                .build());

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

    @Override
    public void tick() { TextInputUtil.whitelistInteger(this.discountInput, 0, 100); }

    void AddPlayer(PlayerReference player)
    {
        this.sendUpdateMessage(this.builder().setCompound("AddPlayer",player.save()));
    }

    void RemovePlayer(PlayerReference player)
    {
        this.sendUpdateMessage(this.builder().setCompound("RemovePlayer",player.save()));
    }

    void PressSetDiscountButton(EasyButton button)
    {
        int discount = TextInputUtil.getIntegerValue(this.discountInput, 1);
        PlayerDiscounts rule = this.getRule();
        if(rule != null)
            rule.setDiscount(discount);
        this.sendUpdateMessage(this.builder().setInt("Discount", discount));
    }

}
