package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.TabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.tabs.TabButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.WidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.IStorageScreenListener;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nullable;

public class TraderStorageScreen extends TabbedMenuScreen<TraderStorageMenu,TraderStorageTab,TraderStorageClientTab<?>> {

    public static final Identifier GUI_TEXTURE = LCApi.id("textures/gui/container/trader.png");

    @Nullable
    public final TraderData getTrader() { return this.menu.getTrader(); }

    public TraderStorageScreen(TraderStorageMenu menu, Inventory inventory) {
        super(menu, inventory,206,236);
        this.inventoryLabelX += TraderStorageMenu.SLOT_OFFSET;
    }

    private final IWidgetPositioner rightPositioner = WidgetPositioner.rightEdge(this,TabButton.SIZE);
    public IWidgetPositioner rightSidePositioner() { return this.rightPositioner; }

    @Override
    protected IWidgetPositioner createTabPositioner() { return WidgetPositioner.leftEdge(this,TabButton.SIZE); }

    @Override
    protected void initialize(ScreenArea area) {
        super.initialize(area);
        this.rightPositioner.clearWidgets();
        this.addChild(this.rightPositioner);
        //Let client attachments add widgets
        for(IStorageScreenListener listener : ClientTraderNode.getClientNodes(this.getTrader(),IStorageScreenListener.class))
            listener.onStorageScreenInit(this);
    }

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
