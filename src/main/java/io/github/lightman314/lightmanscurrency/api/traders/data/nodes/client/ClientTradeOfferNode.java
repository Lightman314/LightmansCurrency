package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientScreenListener;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientPermissionProvider;
import io.github.lightman314.lightmanscurrency.api.traders.client.PermissionOptionsBuilder;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.TradeRulesTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.ITraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.TradeOfferSourceNode;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;

import java.util.function.Consumer;

public class ClientTradeOfferNode<T extends TradeOfferSourceNode<?>> extends ClientTraderNode<T> implements IClientScreenListener, IClientPermissionProvider {

    public ClientTradeOfferNode(T node) { super(node); }

    @Override
    public void addPermissionOptions(TraderData trader, PermissionOptionsBuilder builder) {
        builder.addSimple(Permissions.EDIT_TRADES);
    }

    @Override
    public void onStorageScreenInit(TraderData trader, ITraderStorageScreen screen, Consumer<Object> addWidget) {
        if(!this.node.supportsTradeRules())
            return;
        //Add the Trade Rule button
        IconButton button = IconButton.builder()
                .pressAction(() -> this.PressTradeRulesButton(screen))
                .icon(IconUtil.ICON_TRADE_RULES)
                .addon(EasyAddonHelper.visibleCheck(() -> screen.getMenu().hasPermission(Permissions.EDIT_TRADE_RULES) && screen.getCurrentTab().getTradeRuleTradeIndex() >= 0 && screen.showRightEdgeWidgets()))
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_TRADE_RULES_TRADE))
                .build();
        addWidget.accept(button);
        screen.getRightEdgePositioner().addWidget(button);
    }

    private void PressTradeRulesButton(ITraderStorageScreen screen)
    {
        if(screen.getCurrentTab().getTradeRuleTradeIndex() < 0)
            return;
        screen.ChangeTab(TradeRulesTab.TRADE_KEY, screen.builder().setInt("TradeIndex",screen.getCurrentTab().getTradeRuleTradeIndex()));
    }

}
