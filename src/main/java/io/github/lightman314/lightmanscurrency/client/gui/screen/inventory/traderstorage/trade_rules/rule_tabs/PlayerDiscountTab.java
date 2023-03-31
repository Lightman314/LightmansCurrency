package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerDiscounts;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
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

    Button buttonAddPlayer;
    Button buttonRemovePlayer;

    Button buttonSetDiscount;

    ScrollTextDisplay playerList;

    @Override
    public void onOpen() {

        this.nameInput = this.addWidget(new EditBox(this.font, this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 34, this.screen.getXSize() - 20, 20, EasyText.empty()));

        this.buttonAddPlayer = this.addWidget(EasyButton.builder(EasyText.translatable("gui.button.lightmanscurrency.discount.add"), this::PressAddButton).pos(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 55).size(78, 20).build());
        this.buttonRemovePlayer = this.addWidget(EasyButton.builder(EasyText.translatable("gui.button.lightmanscurrency.discount.remove"), this::PressForgetButton).pos(this.screen.getGuiLeft() + this.screen.getXSize() - 88, this.screen.getGuiTop() + 55).size(78, 20).build());

        this.discountInput = this.addWidget(new EditBox(this.font, this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 9, 20, 20, EasyText.empty()));
        this.discountInput.setMaxLength(2);
        PlayerDiscounts rule = this.getRule();
        if(rule != null)
            this.discountInput.setValue(Integer.toString(rule.getDiscount()));
        this.buttonSetDiscount = this.addWidget(EasyButton.builder(EasyText.translatable("gui.button.lightmanscurrency.discount.set"), this::PressSetDiscountButton).pos(this.screen.getGuiLeft() + 110, this.screen.getGuiTop() + 10).size(50, 20).build());

        this.playerList = this.addWidget(new ScrollTextDisplay(this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 78, this.screen.getXSize() - 14, 61, this.font, this::getPlayerList));
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
    public void onClose() {

    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

        if(getRule() == null)
            return;

        Screen.fill(pose, this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 78, this.screen.getGuiLeft() + this.screen.width - 7, this.screen.getGuiTop() + 78 + 91, 0x000000FF);

        this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.discount.tooltip").getString(), this.discountInput.x + this.discountInput.getWidth() + 4, this.discountInput.y + 3, 0xFFFFFF);


    }

    @Override
    public void tick() {
        TextInputUtil.whitelistInteger(this.discountInput, 0, 100);
    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {

    }

    void PressAddButton(Button button)
    {
        String name = nameInput.getValue();
        if(!name.isBlank())
        {
            nameInput.setValue("");
            CompoundTag updateInfo = new CompoundTag();
            updateInfo.putBoolean("Add", true);
            updateInfo.putString("Name", name);
            this.sendUpdateMessage(updateInfo);
        }
    }

    void PressForgetButton(Button button)
    {
        String name = nameInput.getValue();
        if(!name.isBlank())
        {
            nameInput.setValue("");
            CompoundTag updateInfo = new CompoundTag();
            updateInfo.putBoolean("Add", false);
            updateInfo.putString("Name", name);
            this.sendUpdateMessage(updateInfo);
        }
    }

    void PressSetDiscountButton(Button button)
    {
        int discount = TextInputUtil.getIntegerValue(this.discountInput, 1);
        PlayerDiscounts rule = this.getRule();
        if(rule != null)
            rule.setDiscount(discount);
        CompoundTag updateInfo = new CompoundTag();
        updateInfo.putInt("Discount", discount);
        this.sendUpdateMessage(updateInfo);
    }

}