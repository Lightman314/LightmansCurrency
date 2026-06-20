package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IStorageMenuProvider;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SimpleTradeEditTab;
import io.github.lightman314.lightmanscurrency.api.world.menu.provider.FancyMenuProvider;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabBuilder;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabbedMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.core.LCMenuTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class TraderStorageMenu extends TabbedMenu.Validated<TraderStorageMenu,TraderStorageTab> implements INodeAccess {

    public static final int SLOT_OFFSET = 15;

    private final long traderID;
    @Nullable
    public TraderData getTrader() { return LCApi.getTraderAPI().getTrader(this,this.traderID); }
    public boolean traderValid() {
        TraderData trader = this.getTrader();
        return trader != null && trader.getPermission(this.getPlayer(),BuiltInPermissions.OPEN_STORAGE);
    }

    public TraderStorageMenu(int containerId,Player player,long traderID,MenuValidator validator) {
        this.traderID = traderID;
        super(LCMenuTypes.TRADER_STORAGE.get(),containerId,player,validator);
        this.addCheck(this::traderValid,this::openCustomerMenu);



    }

    @Override
    protected void addInventorySlots(Inventory inventory) {
        //Add Inventory Slots
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, SLOT_OFFSET + 8 + x * 18, 154 + y * 18));
            }
        }
        //Player hotbar
        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(inventory, x, SLOT_OFFSET + 8 + x * 18, 212));
        }
    }

    @Override
    protected int calculateDefaultTab() {
        //Pick the first default tab key
        for(var entry : this.getTabs().entrySet())
        {
            if(entry.getValue().isDefaultTab())
                return entry.getKey();
        }
        return super.calculateDefaultTab();
    }

    public final void changeTab(Identifier tabKey) { this.changeTab(tabKey,FancyPacketMap.EMPTY); }
    public final void changeTab(Identifier tabKey,FancyPacketMap packet) {
        for(TraderStorageTab tab : this.getTabs().values())
        {
            if(tab.getKey().equals(tabKey))
            {
                this.changeTab(tab.getTabSlot(),packet);
                return;
            }
        }
    }

    @Override
    protected void collectTabs(TabBuilder<TraderStorageMenu,TraderStorageTab> builder) {
        TraderData trader = this.getTrader();
        MenuTabBuilder b = new MenuTabBuilder(this,builder);
        if(trader == null)
            b.addTab(SimpleTradeEditTab::new);
        else
        {
            for(IStorageMenuProvider node : trader.getNodes(IStorageMenuProvider.class))
                node.addTabs(b);
        }
    }

    public final TradeContext.Builder getTradeContext() {
        TraderData trader = this.getTrader();
        if(trader == null)
            return TradeContext.builder(new TraderData(LCRegistries.Trader.TRADER_TYPES.getValue(LCApi.id("null"))));
        return TradeContext.builder(trader);
    }

    public void openCustomerMenu() {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.newMutable().setFlag("openCustomer"));
            return;
        }
        TraderData trader = this.getTrader();
        if(trader != null)
            trader.openCustomerMenu(this.getPlayer(),this.getValidator());
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        super.handleMessage(message);
        if(message.contains("openCustomer"))
            this.openCustomerMenu();
    }

    public static MenuProvider getProvider(TraderData trader,MenuValidator validator) {
        return new FancyMenuProvider((id,inv,player) -> new TraderStorageMenu(id,player,trader.getID(),validator),
                buffer -> {
                    buffer.writeLong(trader.getID());IValidatedMenu.encode(buffer,validator);
        });
    }

    @Override
    @Nullable
    public <T extends TraderNode> T getNode(TraderNodeType<T> type) {
        TraderData trader = this.getTrader();
        if(trader != null)
            return trader.getNode(type);
        return null;
    }

    @Override
    public List<TraderNode> getAllNodes() {
        TraderData trader = this.getTrader();
        if(trader != null)
            return trader.getAllNodes();
        return ImmutableList.of();
    }

    private static class MenuTabBuilder implements StorageTabBuilder
    {

        private final TraderStorageMenu menu;
        private final TabBuilder<TraderStorageMenu,TraderStorageTab> builder;

        private MenuTabBuilder(TraderStorageMenu menu,TabBuilder<TraderStorageMenu,TraderStorageTab> builder) {
            this.menu = menu;
            this.builder = builder;
        }

        @Override
        public void addTab(Function<TraderStorageMenu,TraderStorageTab> factory) {
            this.builder.addTab(factory.apply(this.menu));
        }

        private Optional<Map.Entry<Integer,TraderStorageTab>> lookupTab(Identifier tabKey)
        {
            for(var entry : this.builder.previewTabs().entrySet())
            {
                if(entry.getValue().getKey().equals(tabKey))
                    return Optional.of(entry);
            }
            return Optional.empty();
        }

        @Override
        public void removeTab(Identifier tabKey) {
            var entry = this.lookupTab(tabKey);
            if(entry.isPresent())
                this.builder.removeTab(entry.get().getKey());
        }

        @Override
        public boolean hasTab(Identifier tabKey) { return this.lookupTab(tabKey).isPresent(); }

    }

}