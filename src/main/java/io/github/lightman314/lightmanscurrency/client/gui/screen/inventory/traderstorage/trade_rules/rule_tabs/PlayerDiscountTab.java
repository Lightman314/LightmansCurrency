package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerDiscounts;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public class PlayerDiscountTab extends TradeRuleSubTab<PlayerDiscounts> {


    public PlayerDiscountTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, PlayerDiscounts.TYPE); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_DISCOUNT_LIST; }

    EditBox nameInput;
    EditBox discountInput;

    EasyButton buttonAddPlayer;
    EasyButton buttonRemovePlayer;

    EasyButton buttonSetDiscount;

    ScrollTextDisplay playerList;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.nameInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 10, screenArea.y + 34, screenArea.width - 20, 20, EasyText.empty()));

        this.buttonAddPlayer = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(10,55))
                .width(78)
                .text(LCText.BUTTON_ADD)
                .pressAction(this::PressAddButton)
                .build());
        this.buttonRemovePlayer = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 88, 55))
                .width(78)
                .text(LCText.BUTTON_REMOVE)
                .pressAction(this::PressForgetButton)
                .build());

        this.discountInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 10, screenArea.y + 9, 20, 20, EasyText.empty()));
        this.discountInput.setMaxLength(2);
        PlayerDiscounts rule = this.getRule();
        if(rule != null)
            this.discountInput.setValue(Integer.toString(rule.getDiscount()));
        this.buttonSetDiscount = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(110,10))
                .width(50)
                .text(LCText.BUTTON_SET)
                .pressAction(this::PressSetDiscountButton)
                .build());

        this.playerList = this.addChild(ScrollTextDisplay.builder()
                .position(screenArea.pos.offset(7,78))
                .size(screenArea.width - 14,61)
                .text(this::getPlayerList)
                .columns(2)
                .build());

    }

    private List<Component> getPlayerList()
    {
        List<Component> playerList = Lists.newArrayList();
        PlayerDiscounts rule = this.getRule();
        if(rule == null)
            return playerList;
        for(PlayerReference player : rule.getPlayerList())
            playerList.add(player.getNameComponent(true));
        return playerList;
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        if(getRule() == null)
            return;

        gui.pushOffset(this.discountInput);
        gui.drawString(LCText.GUI_PLAYER_DISCOUNTS_INFO.get(), this.discountInput.getWidth() + 4, 3, 0xFFFFFF);
        gui.popOffset();

    }

    @Override
    public void tick() { TextInputUtil.whitelistInteger(this.discountInput, 0, 100); }

    void PressAddButton(EasyButton button)
    {
        String name = nameInput.getValue();
        if(!name.isBlank())
        {
            nameInput.setValue("");
            this.sendUpdateMessage(this.builder()
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
            this.sendUpdateMessage(this.builder()
                    .setBoolean("Add", false)
                    .setString("Name", name));
        }
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