package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.NodeSettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.AdminNode;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class CreativeSettingsTab extends NodeSettingsSubTab<AdminNode> {

    public CreativeSettingsTab(TraderSettingsClientTab parent) { super(AdminNode.TYPE,parent); }

    EasyButton buttonToggleCreative;
    EasyButton buttonAddTrade;
    EasyButton buttonRemoveTrade;

    PlainButton buttonToggleStoreMoney;

    @Override
    public IconData getIcon() { return IconUtil.ICON_CREATIVE; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_CREATIVE.get(); }

    @Override
    public boolean canOpen(AdminNode node) { return LCAdminMode.isAdminPlayer(this.menu.getPlayer()); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        //Creative Toggle
        this.buttonToggleCreative = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(35,20))
                .width(screenArea.width - 70)
                .pressAction(this::ToggleCreative)
                .text(this::getCreativeButtonText)
                .build());
        this.buttonAddTrade = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(45,60))
                .width(screenArea.width - 90)
                .pressAction(this::AddTrade)
                .text(LCText.BUTTON_TRADER_SETTINGS_CREATIVE_ADD_TRADE)
                .build());
        this.buttonRemoveTrade = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(45,82))
                .width(screenArea.width - 90)
                .pressAction(this::RemoveTrade)
                .text(LCText.BUTTON_TRADER_SETTINGS_CREATIVE_REMOVE_TRADE)
                .build());

        this.buttonToggleStoreMoney = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(35,110))
                .pressAction(this::ToggleStoreMoney)
                .sprite(SpriteUtil.createCheckbox(this::storedCreativeMoney))
                .build());

    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        //Draw current trade count
        TextRenderUtil.drawCenteredText(gui,LCText.GUI_TRADER_SETTINGS_CREATIVE_TRADE_COUNT.get(trader.getTradeCount()), this.screen.getXSize() / 2, 50, 0x404040);

        //Draw "Store Creative Money" label
        gui.drawString(LCText.GUI_TRADER_SETTINGS_CREATIVE_STORE_MONEY.get(), 47, 111, 0x404040);

    }

    private boolean isTraderCreative()
    {
        AdminNode node = this.getNode();
        return node != null && node.isCreative();
    }

    private Component getCreativeButtonText() {
        return this.isTraderCreative() ? LCText.BUTTON_TRADER_SETTINGS_CREATIVE_ENABLED.getWithStyle(ChatFormatting.YELLOW) : LCText.BUTTON_TRADER_SETTINGS_CREATIVE_DISABLED.get();
    }

    private boolean storedCreativeMoney() {
        AdminNode node = this.getNode();
        return node != null && node.shouldStoreCreativeMoney();
    }

    private void ToggleCreative()
    {
        AdminNode node = this.getNode();
        if(node == null)
            return;
        this.sendMessage(this.builder().setBoolean("MakeCreative", !node.isCreative()));
    }

    private void ToggleStoreMoney()
    {
        AdminNode node = this.getNode();
        if(node == null)
            return;
        this.sendMessage(this.builder().setBoolean("StoreCreativeMoney", !node.shouldStoreCreativeMoney()));
    }

    private void AddTrade()
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        this.sendMessage(this.builder().setFlag("AddTrade"));
    }

    private void RemoveTrade()
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        this.sendMessage(this.builder().setFlag("RemoveTrade"));
    }


}
