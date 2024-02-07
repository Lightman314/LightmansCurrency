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
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerDiscounts;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public class PlayerDiscountTab extends TradeRuleSubTab<PlayerDiscounts> {


    public PlayerDiscountTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, PlayerDiscounts.TYPE); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_DISCOUNT_LIST; }

    EditBox nameInput;
    EditBox discountInput;

    EasyButton buttonAddPlayer;
    EasyButton buttonRemovePlayer;

    EasyButton buttonSetDiscount;

    ScrollTextDisplay playerList;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.nameInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 10, screenArea.y + 34, screenArea.width - 20, 20, EasyText.empty()));

        this.buttonAddPlayer = this.addChild(new EasyTextButton(screenArea.pos.offset( 10, 55), 78, 20, EasyText.translatable("gui.button.lightmanscurrency.discount.add"), this::PressAddButton));
        this.buttonRemovePlayer = this.addChild(new EasyTextButton(screenArea.pos.offset(screenArea.width - 88, 55), 78, 20, EasyText.translatable("gui.button.lightmanscurrency.discount.remove"), this::PressForgetButton));

        this.discountInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 10, screenArea.y + 9, 20, 20, EasyText.empty()));
        this.discountInput.setMaxLength(2);
        PlayerDiscounts rule = this.getRule();
        if(rule != null)
            this.discountInput.setValue(Integer.toString(rule.getDiscount()));
        this.buttonSetDiscount = this.addChild(new EasyTextButton(screenArea.pos.offset(110, 10), 50, 20, EasyText.translatable("gui.button.lightmanscurrency.discount.set"), this::PressSetDiscountButton));

        this.playerList = this.addChild(new ScrollTextDisplay(screenArea.pos.offset(7, 78), screenArea.width - 14, 61, this::getPlayerList));
        this.playerList.setColumnCount(2);

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
        gui.drawString(EasyText.translatable("gui.lightmanscurrency.discount.tooltip").getString(), this.discountInput.getWidth() + 4, 3, 0xFFFFFF);
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

    void PressSetDiscountButton(EasyButton button)
    {
        int discount = TextInputUtil.getIntegerValue(this.discountInput, 1);
        PlayerDiscounts rule = this.getRule();
        if(rule != null)
            rule.setDiscount(discount);
        this.sendUpdateMessage(LazyPacketData.simpleInt("Discount", discount));
    }

}
