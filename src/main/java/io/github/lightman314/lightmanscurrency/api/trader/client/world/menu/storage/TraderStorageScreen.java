package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.TabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.tabs.TabButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.WidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.IStorageScreenListener;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.customer.TraderCustomerScreen;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nullable;

public class TraderStorageScreen extends TabbedMenuScreen<TraderStorageMenu,TraderStorageTab,TraderStorageClientTab<?>> {

    public static final Identifier GUI_TEXTURE = LCApi.id("textures/gui/container/trader_storage.png");

    @Nullable
    public final TraderData getTrader() { return this.menu.getTrader(); }

    public TraderStorageScreen(TraderStorageMenu menu, Inventory inventory) {
        super(menu, inventory, TraderCustomerScreen.WIDTH,TraderCustomerScreen.HEIGHT);
        this.inventoryLabelX += TraderStorageMenu.SLOT_OFFSET;
    }

    private IWidgetPositioner rightPositioner = WidgetPositioner.rightEdge(this,IconButton.SIZE);
    public IWidgetPositioner rightSidePositioner() { return this.rightPositioner; }

    @Override
    protected IWidgetPositioner createTabPositioner() { return WidgetPositioner.leftEdge(this,TabButton.SIZE); }

    @Override
    protected void initialize(ScreenArea area) {
        super.initialize(area);
        this.rightPositioner = this.addChild(WidgetPositioner.rightEdge(this,IconButton.SIZE));

        //Add additional buttons
        IconButton returnToCustomerMenu = this.addChild(IconButton.builder()
                .onPress(this.getMenu()::openCustomerMenu)
                .withIcon(ItemIcon.of(LCItems.TRADING_CORE))
                .tooltip(TooltipSource.simple(TraderStorageMenu.TOOLTIP_TRADER_OPEN_TRADES))
                .visible(this::showRightEdgeWidgets)
                .build());

        this.rightPositioner.addWidgets(returnToCustomerMenu);

        //Let client attachments add widgets
        for(IStorageScreenListener listener : ClientTraderNode.getClientNodes(this.getTrader(),IStorageScreenListener.class))
            listener.onStorageScreenInit(this);

    }

    public boolean showRightEdgeWidgets() { return this.getCurrentTab().showRightEdgeWidgets(); }

    @Override
    protected void extractBackground(FancyGuiExtractor gui,ScreenArea area) {
        //Render the normal background
        gui.blitBackground(GUI_TEXTURE,area);
        //Render the tab background
        super.extractBackground(gui,area);
        //Render any client attachments
        for(IStorageScreenListener listener : ClientTraderNode.getClientNodes(this.getTrader(),IStorageScreenListener.class))
            listener.onStorageScreenRender(this,gui,area);
    }

}
