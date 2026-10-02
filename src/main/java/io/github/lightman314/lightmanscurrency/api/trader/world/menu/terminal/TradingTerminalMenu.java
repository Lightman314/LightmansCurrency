package io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.TrackingLevel;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.managers.MenuTrackingManager;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.builtin.NetworkTraderSelectionTab;
import io.github.lightman314.lightmanscurrency.api.world.menu.provider.FancyMenuProvider;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabBuilder;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabbedMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.core.LCMenuTypes;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import java.util.List;

public class TradingTerminalMenu extends TabbedMenu.Validated<TradingTerminalMenu,TradingTerminalTab> implements TraderSource.Multi {

    private final MenuTrackingManager tracking;

    public TradingTerminalMenu(int containerId,Player player,MenuValidator validator) {
        this(LCMenuTypes.TRADING_TERMINAL.get(),containerId, player,validator);
    }
    protected TradingTerminalMenu(MenuType<?> type, int containerId, Player player,MenuValidator validator) {
        super(type,containerId,player,validator);
        //Flag all future interactions as being through the network terminal
        validator.flagAsNetworkAccess();
        this.tracking = new MenuTrackingManager(this,player,TrackingLevel.CUSTOMER);
    }

    @Override
    public List<TraderData> getTraders() { return LCApi.getTraderAPI().getAllNetworkTraders(this); }

    //No slots within this menu
    @Override
    protected void addInventorySlots(Inventory inventory) {}

    @Override
    public void serverTick() {
        super.serverTick();
        this.tracking.tick();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.tracking.onClose();
    }

    @Override
    protected void collectTabs(TabBuilder<TradingTerminalMenu,TradingTerminalTab> builder) {
        builder.addTab(new NetworkTraderSelectionTab(this));
    }

    public static MenuProvider createProvider(MenuValidator validator) {
        return new FancyMenuProvider((id,inv,player) -> new TradingTerminalMenu(id,player,validator),buf -> MenuValidator.STREAM_CODEC.encode(buf,validator));
    }

}
