package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.MoneyStorageTab;
import net.minecraft.network.chat.Component;

public class MoneyStorageClientTab extends TraderStorageClientTab<MoneyStorageTab> {

    public static final ClientMenuTab.TabBuilder<TraderStorageMenu,MoneyStorageTab,TraderStorageTab,TraderStorageScreen> BUILDER = MoneyStorageClientTab::new;

    protected MoneyStorageClientTab(TraderStorageMenu menu,MoneyStorageTab commonTab,TraderStorageScreen screen) { super(menu,commonTab,screen); }

    @Override
    public IconData getIcon() { return SpriteIcon.of(LCApi.id("icon/store_coins")); }

    @Override
    public Component getName() { return MoneyStorageTab.TOOLTIP_MONEY_STORAGE.get(); }

    private MoneyValueWidget amountWidget;

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {

        this.amountWidget = this.addChild(MoneyValueWidget.builder()
                .atPos(area.centerX() - MoneyValueWidget.HALF_WIDTH,area.y + 10)
                .old(this.amountWidget)
                .allowFreeInput(false)
                .build());

        //Store Button
        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(22,85))
                .ofWidth(80)
                .withText(MoneyStorageTab.BUTTON_TRADER_STORE_MONEY)
                .onPress(this::storeMoney)
                .active(this::storeButtonActive)
                .build());

        //Collect Button
        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(area.width - 102,85))
                .ofWidth(80)
                .withText(MoneyStorageTab.BUTTON_TRADER_COLLECT_MONEY)
                .onPress(this::collectMoney)
                .active(this::collectButtonActive)
                .build());

    }

    private boolean storeButtonActive() {
        if(!this.getCommonTab().canStoreMoney())
            return false;
        MoneyResourceHandler handler = this.getCommonTab().getMoneyResources();
        return !handler.isEmpty() || this.amountWidget != null && !this.amountWidget.getCurrentValue().isEmpty();
    }

    private boolean collectButtonActive() {
        if(!this.getCommonTab().canCollectMoney())
            return false;
        MoneyStorageNode node = this.getNode(MoneyStorageNode.TYPE);
        return node != null && !node.getStorage().isEmpty();
    }

    private void storeMoney() {
        if(this.amountWidget == null)
            return;
        this.getCommonTab().storeMoney(this.amountWidget.getCurrentValue());
        this.amountWidget.changeValue(MoneyValue.empty());
    }

    private void collectMoney() {
        if(this.amountWidget == null)
            return;
        this.getCommonTab().collectMoney(this.amountWidget.getCurrentValue());
        this.amountWidget.changeValue(MoneyValue.empty());
    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        gui.blitSlots(this.getCommonTab().getSlots());
        //Render current balance
        MoneyStorageNode node = this.getNode(MoneyStorageNode.TYPE);
        if(node != null)
            gui.centeredText(MoneyStorageTab.GUI_TRADER_MONEY_STORAGE_CONTENTS.get(MoneyDisplayHelper.getCyclingValueLine(node.getStorage())),area.halfWidth(),110,0xFF404040,false);
    }

}
