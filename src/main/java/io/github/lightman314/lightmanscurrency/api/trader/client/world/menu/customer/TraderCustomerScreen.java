package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.customer;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.TabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.tabs.TabButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.WidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.EmptyMoneyResource;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.SortedMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.AbstractTabbedCustomerMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.TraderCustomerAccess;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.TraderCustomerMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.TraderCustomerTab;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class TraderCustomerScreen extends TabbedMenuScreen<AbstractTabbedCustomerMenu,TraderCustomerTab,TraderCustomerClientTab<?>> implements TraderCustomerAccess {

    public static final Identifier GUI_TEXTURE = LCApi.id("textures/gui/container/trader.png");

    public static final int WIDTH = 210;
    public static final int HEIGHT = 236;

    private final ScreenPosition INFO_WIDGET_POSITION = ScreenPosition.of(AbstractTabbedCustomerMenu.SLOT_OFFSET + 160,HEIGHT - 96);

    private final IWidgetPositioner rightEdgePositioner = WidgetPositioner.rightEdge(this,20);
    public IWidgetPositioner getRightEdgePositioner() { return this.rightEdgePositioner; }

    @Override
    public final TraderSource getTraderSource() { return this.getMenu().getTraderSource(); }

    public TraderCustomerScreen(AbstractTabbedCustomerMenu menu, Inventory inventory) {
        super(menu,inventory,WIDTH,HEIGHT);
        this.inventoryLabelX += AbstractTabbedCustomerMenu.SLOT_OFFSET;
    }

    @Override
    protected IWidgetPositioner createTabPositioner() { return WidgetPositioner.leftEdge(this,TabButton.SIZE); }

    @Override
    protected void initialize(ScreenArea area) {
        super.initialize(area);
        this.rightEdgePositioner.clearWidgets();
        this.addChild(this.rightEdgePositioner);

        IconButton openStorageButton = this.addChild(IconButton.builder()
                .onPress(this.getMenu()::openStorage)
                .withIcon(ItemIcon.of(Items.CHEST))
                .visible(this.getMenu()::canOpenStorage)
                .tooltip(TooltipSource.simple(TraderCustomerMenu.TOOLTIP_TRADER_OPEN_STORAGE))
                .build());
        IconButton collectMoneyButton = this.addChild(IconButton.builder()
                .onPress(this.getMenu()::quickCollectMoney)
                .withIcon(SpriteIcon.of(LCApi.id("icon/collect_coins")))
                .visible(this.getMenu()::canQuickCollectMoney)
                .active(hasMoneyToCollect(this.getTraderSource()))
                .tooltip(TooltipSource.deferredList(collectMoneyTooltip(this.getTraderSource())))
                .build());
        IconButton openTerminalButton = this.addChild(IconButton.builder()
                .onPress(this.getMenu()::openTerminal)
                .withIcon(SpriteIcon.of(LCApi.id("icon/arrow_back")))
                .visible(this.getMenu()::canOpenTerminal)
                .tooltip(TooltipSource.simple(TraderCustomerMenu.TOOLTIP_TRADER_NETWORK_BACK))
                .build());
        this.rightEdgePositioner.addWidgets(openStorageButton,collectMoneyButton);

    }

    @Override
    protected void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        gui.blitBackground(GUI_TEXTURE,area);
        super.extractBackground(gui, area);
        if(this.getCurrentTab().showMoneyInfo()) {
            //Render the Info Sprite
            gui.blitSprite(LCSprites.GENERIC_INFO,INFO_WIDGET_POSITION.x,INFO_WIDGET_POSITION.y);
            //Need the trade context to render available funds info
            try(TradeContext context = this.menu.getContext(null).build()) {
                SortedMoneyResourceHandler money = context.getCustomerResource(BuiltInResourceTypes.MONEY);
                //Render the available funds text
                Component line = MoneyDisplayHelper.getCyclingValueLine(money,Component.empty());
                gui.text(line,INFO_WIDGET_POSITION.x - gui.getFont().width(line),INFO_WIDGET_POSITION.y + 2,0xFF404040,false);
                //Now render the full tooltip if hovered
                if(LCSprites.GENERIC_INFO.getArea(INFO_WIDGET_POSITION.offset(this.getCorner())).isInArea(gui.getMousePos())) {
                    List<Component> tooltip = money.getTooltips();
                    if(!tooltip.isEmpty())
                        gui.renderTooltipAtMouse(tooltip);
                }
            }
        }
    }

    public static BooleanSupplier hasMoneyToCollect(TraderSource traderSource) {
        return () -> {
            TraderData trader = traderSource.getSimpleTrader();
            return trader != null && !trader.getNodeValue(MoneyStorageNode.TYPE,MoneyStorageNode::getStorage,EmptyMoneyResource.INSTANCE).isEmpty();
        };
    }

    public static Supplier<List<Component>> collectMoneyTooltip(TraderSource traderSource) {
        return () -> {
            TraderData trader = traderSource.getSimpleTrader();
            if(trader != null) {
                MoneyStorageNode node = trader.getNode(MoneyStorageNode.TYPE);
                if(node == null)
                    return List.of();
                //TODO ignore if bank node is linked to the bank account
                MoneyResourceHandler storage = node.getStorage();
                if(!storage.isEmpty()) {
                    List<Component> result = new ArrayList<>();
                    result.add(TraderCustomerMenu.TOOLTIP_TRADER_COLLECT_MONEY.get());
                    result.addAll(MoneyDisplayHelper.contentsAsMultiLineText(storage));
                    return result;
                }
            }
            return List.of();
        };
    }

}
